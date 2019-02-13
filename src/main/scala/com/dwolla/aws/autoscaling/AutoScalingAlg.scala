package com.dwolla.aws.autoscaling

import cats._
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

import scala.concurrent.duration._

abstract class AutoScalingAlg[F[_] : Monad] {
  def pauseAndRecurse(topic: SnsTopicArn, lifecycleHookNotification: LifecycleHookNotification): F[Unit]
  def continueAutoScaling(l: LifecycleHookNotification): F[Unit]
}

object AutoScalingAlg {
  def apply[F[_] : Effect : Timer](autoScalingClient: AmazonAutoScalingAsync, snsClient: AmazonSNSAsync): AutoScalingAlg[F] =
    new AutoScalingAlgImpl(autoScalingClient: AmazonAutoScalingAsync, snsClient: AmazonSNSAsync)
}

class AutoScalingAlgImpl[F[_] : Async : Timer](autoScalingClient: AmazonAutoScalingAsync,
                                               snsClient: AmazonSNSAsync) extends AutoScalingAlg[F] {
  override def pauseAndRecurse(t: SnsTopicArn, l: LifecycleHookNotification): F[Unit] = {
    val req = new PublishRequest()
      .withTopicArn(t)
      .withMessage(l.asJson.noSpaces)

    Timer[F].sleep(5.seconds) >> req.executeVia[F](snsClient.publishAsync).void
  }

  override def continueAutoScaling(l: LifecycleHookNotification): F[Unit] = {
    val req = new CompleteLifecycleActionRequest()
      .withLifecycleHookName(l.lifecycleHookName)
      .withAutoScalingGroupName(l.autoScalingGroupName)
      .withLifecycleActionResult("CONTINUE")
      .withInstanceId(l.EC2InstanceId)

    req.executeVia[F](autoScalingClient.completeLifecycleActionAsync).void
  }

}
