package com.dwolla.aws.autoscaling

import cats.effect.*
import cats.effect.syntax.all.*
import cats.syntax.all.*
import com.dwolla.aws.autoscaling.LifecycleState.*
import com.dwolla.aws.sns.*
import com.dwolla.aws.ec2.*
import io.circe.syntax.*
import org.typelevel.log4cats.{Logger, LoggerFactory}
import software.amazon.awssdk.services.autoscaling.*
import software.amazon.awssdk.services.autoscaling.model.{CompleteLifecycleActionRequest, DescribeAutoScalingInstancesRequest}

import scala.concurrent.duration.*
import scala.jdk.CollectionConverters.*

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
  private def getInstanceLifecycleState(ec2Instance: Ec2InstanceId): F[Option[LifecycleState]] =
    LoggerFactory[F].create.flatMap { implicit logger =>
      for {
        _ <- Logger[F].info(s"checking lifecycle state for instance $ec2Instance")
        req = DescribeAutoScalingInstancesRequest.builder().instanceIds(ec2Instance.value).build()
        resp <- Async[F].fromCompletableFuture(Sync[F].delay(autoScalingClient.describeAutoScalingInstances(req)))
      } yield
        resp
          .autoScalingInstances()
          .asScala
          .collectFirst {
            case instance if Ec2InstanceId(instance.instanceId()) == ec2Instance =>
              LifecycleState.fromString(instance.lifecycleState())
          }
          .flatten
    }
  
  override def pauseAndRecurse(t: SnsTopicArn, l: LifecycleHookNotification): F[Unit] = {
    val sleepDuration = 5.seconds

    LoggerFactory[F].create.flatMap { implicit logger =>
      Logger[F].info(s"Sleeping for $sleepDuration, then restarting Lambda") >>
        getInstanceLifecycleState(l.EC2InstanceId)
          .map(_.contains(PendingWait))
          .ifM(
            sns.publish(t, l.asJson.noSpaces).delayBy(sleepDuration),
            Logger[F].info("Instance not in PendingWait status; refusing to republish the SNS message")
          )
    }
  }

  override def continueAutoScaling(l: LifecycleHookNotification): F[Unit] = {
    val req = CompleteLifecycleActionRequest.builder()
      .lifecycleHookName(l.lifecycleHookName.value)
      .autoScalingGroupName(l.autoScalingGroupName.value)
      .lifecycleActionResult("CONTINUE")
      .instanceId(l.EC2InstanceId.value)
      .build()

    LoggerFactory[F].create.flatMap(_.info(s"continuing auto scaling for ${l.autoScalingGroupName}")) >>
      Async[F].fromCompletableFuture(Sync[F].delay(autoScalingClient.completeLifecycleAction(req))).void
  }

}
