package com.dwolla.aws.autoscaling

import cats.effect.*
import cats.effect.syntax.all.*
import cats.syntax.all.*
import com.dwolla.aws.sns.*
import io.circe.syntax.*
import org.typelevel.log4cats.{Logger, LoggerFactory}
import software.amazon.awssdk.services.autoscaling.*
import software.amazon.awssdk.services.autoscaling.model.CompleteLifecycleActionRequest

import scala.concurrent.duration.*

trait AutoScalingAlg[F[_]] {
  def pauseAndRecurse(topic: SnsTopicArn, lifecycleHookNotification: LifecycleHookNotification): F[Unit]
  def continueAutoScaling(l: LifecycleHookNotification): F[Unit]
}

object AutoScalingAlg {
  def apply[F[_] : Async : LoggerFactory](autoScalingClient: AutoScalingAsyncClient,
                                          sns: SnsAlg[F]): AutoScalingAlg[F] =
    new AutoScalingAlgImpl(autoScalingClient, sns)
}

class AutoScalingAlgImpl[F[_] : Async : LoggerFactory](autoScalingClient: AutoScalingAsyncClient,
                                                       sns: SnsAlg[F]) extends AutoScalingAlg[F] {
  override def pauseAndRecurse(t: SnsTopicArn, l: LifecycleHookNotification): F[Unit] = {
    val sleepDuration = 5.seconds

    LoggerFactory[F].create.flatMap { implicit logger =>
      Logger[F].info(s"Sleeping for $sleepDuration, then restarting Lambda") >>
        sns.publish(t, l.asJson.noSpaces)
          .delayBy(sleepDuration)
    }
  }

  override def continueAutoScaling(l: LifecycleHookNotification): F[Unit] = {
    val req = CompleteLifecycleActionRequest.builder()
      .lifecycleHookName(l.lifecycleHookName)
      .autoScalingGroupName(l.autoScalingGroupName)
      .lifecycleActionResult("CONTINUE")
      .instanceId(l.EC2InstanceId.value)
      .build()

    LoggerFactory[F].create.flatMap(_.info(s"continuing auto scaling for ${l.autoScalingGroupName}")) >>
      Async[F].fromCompletableFuture(Sync[F].delay(autoScalingClient.completeLifecycleAction(req))).void
  }

}
