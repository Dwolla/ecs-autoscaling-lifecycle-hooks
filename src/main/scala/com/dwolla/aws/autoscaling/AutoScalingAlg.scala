package com.dwolla.aws.autoscaling

import cats._
import cats.effect._
import cats.implicits._
import com.amazonaws.services.autoscaling.AmazonAutoScalingAsync
import com.amazonaws.services.autoscaling.model.CompleteLifecycleActionRequest
import com.amazonaws.services.sns.AmazonSNSAsync
import com.dwolla.aws.autoscaling.model.LifecycleHookNotification
import com.dwolla.fs2aws._

import scala.concurrent.duration._

abstract class AutoScalingAlg[F[_] : Monad] {
  def pauseAndRecurse: F[Unit]
  def continueAutoScaling(l: LifecycleHookNotification): F[Unit]
}

object AutoScalingAlg {
  def apply[F[_] : Effect : Timer](autoScalingClient: AmazonAutoScalingAsync, snsClient: AmazonSNSAsync): AutoScalingAlg[F] =
    new AutoScalingAlgImpl(autoScalingClient: AmazonAutoScalingAsync, snsClient: AmazonSNSAsync)
}

class AutoScalingAlgImpl[F[_] : Async : Timer](autoScalingClient: AmazonAutoScalingAsync,
                                               snsClient: AmazonSNSAsync) extends AutoScalingAlg[F] {
  override def pauseAndRecurse: F[Unit] =
    Timer[F].sleep(5.seconds) >> MonadError[F, Throwable].raiseError(new RuntimeException("finish me")) // TODO finish this

  override def continueAutoScaling(l: LifecycleHookNotification): F[Unit] = {
    val req = new CompleteLifecycleActionRequest()
      .withLifecycleHookName(l.lifecycleHookName)
      .withAutoScalingGroupName(l.autoScalingGroupName)
      .withLifecycleActionResult("CONTINUE")
      .withInstanceId(l.EC2InstanceId)

    req.executeVia[F](autoScalingClient.completeLifecycleActionAsync).void
  }

}
