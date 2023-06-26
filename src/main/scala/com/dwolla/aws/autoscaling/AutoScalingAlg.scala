package com.dwolla.aws.autoscaling

import cats.effect.*
import cats.syntax.all.*
import cats.effect.syntax.all.*
import io.circe.syntax.*
import software.amazon.awssdk.services.autoscaling.model.CompleteLifecycleActionRequest
import software.amazon.awssdk.services.sns.model.PublishRequest

import scala.concurrent.duration.*
import com.dwolla.aws.autoscaling.model.LifecycleHookNotification
import com.dwolla.aws.sns.model.SnsTopicArn
import org.typelevel.log4cats.{Logger, LoggerFactory}
import software.amazon.awssdk.services.autoscaling.AutoScalingAsyncClient
import software.amazon.awssdk.services.sns.SnsAsyncClient

trait AutoScalingAlg[F[_]] {
  def pauseAndRecurse(topic: SnsTopicArn, lifecycleHookNotification: LifecycleHookNotification): F[Unit]
  def continueAutoScaling(l: LifecycleHookNotification): F[Unit]
}

object AutoScalingAlg {
  def apply[F[_] : Async : LoggerFactory](autoScalingClient: AutoScalingAsyncClient,
                                          snsClient: SnsAsyncClient): AutoScalingAlg[F] =
    new AutoScalingAlgImpl(autoScalingClient, snsClient)
}

class AutoScalingAlgImpl[F[_] : Async : LoggerFactory](autoScalingClient: AutoScalingAsyncClient,
                                                       snsClient: SnsAsyncClient) extends AutoScalingAlg[F] {
  override def pauseAndRecurse(t: SnsTopicArn, l: LifecycleHookNotification): F[Unit] = {
    val req = PublishRequest.builder()
      .topicArn(t.value)
      .message(l.asJson.noSpaces)
      .build()

    val sleepDuration = 5.seconds

    LoggerFactory[F].create.flatMap { implicit l =>
      Logger[F].info(s"Sleeping for $sleepDuration, then restarting Lambda") >>
        Async[F]
          .fromCompletableFuture {
            Sync[F]
              .delay(snsClient.publish(req))
              .delayBy(sleepDuration)
          }
          .void
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
