package com.dwolla.aws.autoscaling

import cats.effect._
import cats.implicits._
import com.amazonaws.services.autoscaling.AmazonAutoScalingAsync
import com.amazonaws.services.autoscaling.model.CompleteLifecycleActionRequest
import com.amazonaws.services.sns.AmazonSNSAsync
import com.amazonaws.services.sns.model.PublishRequest
import com.dwolla.aws.autoscaling.model.LifecycleHookNotification
import com.dwolla.aws.sns.model.SnsTopicArn
import com.dwolla.fs2aws._
import io.circe.syntax._
import io.chrisdavenport.log4cats._

import scala.concurrent.duration._

trait AutoScalingAlg[F[_]] {
  def pauseAndRecurse(topic: SnsTopicArn, lifecycleHookNotification: LifecycleHookNotification): F[Unit]
  def continueAutoScaling(l: LifecycleHookNotification): F[Unit]
}

object AutoScalingAlg {
  def apply[F[_] : Effect : Timer : Logger](autoScalingClient: AmazonAutoScalingAsync,
                                            snsClient: AmazonSNSAsync): AutoScalingAlg[F] =
    new AutoScalingAlgImpl(autoScalingClient: AmazonAutoScalingAsync, snsClient: AmazonSNSAsync)
}

class AutoScalingAlgImpl[F[_] : Async : Timer : Logger](autoScalingClient: AmazonAutoScalingAsync,
                                                        snsClient: AmazonSNSAsync) extends AutoScalingAlg[F] {
  override def pauseAndRecurse(t: SnsTopicArn, l: LifecycleHookNotification): F[Unit] = {
    val req = new PublishRequest()
      .withTopicArn(t)
      .withMessage(l.asJson.noSpaces)

    val sleepDuration = 5.seconds

    for {
      _ <- Logger[F].info(s"Sleeping for $sleepDuration, then restarting Lambda")
      _ <- Timer[F].sleep(sleepDuration)
      _ <- req.executeVia[F](snsClient.publishAsync).void
    } yield ()
  }

  override def continueAutoScaling(l: LifecycleHookNotification): F[Unit] = {
    val req = new CompleteLifecycleActionRequest()
      .withLifecycleHookName(l.lifecycleHookName)
      .withAutoScalingGroupName(l.autoScalingGroupName)
      .withLifecycleActionResult("CONTINUE")
      .withInstanceId(l.EC2InstanceId)

    Logger[F].info(s"continuing auto scaling for ${l.autoScalingGroupName}") >> req.executeVia[F](autoScalingClient.completeLifecycleActionAsync).void
  }

}
