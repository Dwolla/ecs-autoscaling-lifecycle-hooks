package com.dwolla.aws.autoscaling

import cats.effect.*
import cats.effect.syntax.all.*
import cats.syntax.all.*
import com.amazonaws.autoscaling.{AutoScaling, LifecycleActionResult, XmlStringMaxLen19}
import com.amazonaws.ec2.InstanceId
import com.amazonaws.sns.*
import com.dwolla.aws.autoscaling.LifecycleState.*
import com.dwolla.aws.sns.*
import io.circe.syntax.*
import org.typelevel.log4cats.{Logger, LoggerFactory}

import scala.concurrent.duration.*

trait AutoScalingAlg[F[_]] {
  def pauseAndRecurse(topic: TopicARN,
                      lifecycleHookNotification: LifecycleHookNotification,
                      onlyIfInState: LifecycleState,
                     ): F[Unit]
  def continueAutoScaling(l: LifecycleHookNotification): F[Unit]
}

object AutoScalingAlg {
  def apply[F[_] : Async : LoggerFactory](autoScalingClient: AutoScaling[F],
                                          sns: SnsAlg[F]): AutoScalingAlg[F] =
    new AutoScalingAlgImpl(autoScalingClient, sns)
}

extension (i: InstanceId) {
  def forAutoScaling: XmlStringMaxLen19 = XmlStringMaxLen19(i.value)
}

class AutoScalingAlgImpl[F[_] : Async : LoggerFactory](autoScalingClient: AutoScaling[F],
                                                       sns: SnsAlg[F]) extends AutoScalingAlg[F] {
  private def getInstanceLifecycleState(ec2Instance: InstanceId): F[Option[LifecycleState]] =
    LoggerFactory[F].create.flatMap { case given Logger[F] =>
      for {
        _ <- Logger[F].info(s"checking lifecycle state for instance $ec2Instance")
        resp <- autoScalingClient.describeAutoScalingInstances(ec2Instance.forAutoScaling.pure[List].some)
      } yield
        resp
          .autoScalingInstances
          .flatMap {
            _.collectFirstSome {
              case instance if InstanceId(instance.instanceId.value) == ec2Instance =>
                LifecycleState.fromString(instance.lifecycleState.value)
              case _ => None
            }
          }
    }
  
  override def pauseAndRecurse(t: TopicARN,
                               l: LifecycleHookNotification,
                               onlyIfInState: LifecycleState,
                              ): F[Unit] = {
    val sleepDuration = 5.seconds

    LoggerFactory[F].create.flatMap { case given Logger[F] =>
      Logger[F].info(s"Sleeping for $sleepDuration, then restarting Lambda") >>
        getInstanceLifecycleState(l.EC2InstanceId)
          .map(_.contains(onlyIfInState))
          .ifM(
            sns.publish(t, Message(l.asJson.noSpaces)).delayBy(sleepDuration),
            Logger[F].info("Instance not in PendingWait status; refusing to republish the SNS message")
          )
    }
  }

  override def continueAutoScaling(l: LifecycleHookNotification): F[Unit] =
    LoggerFactory[F].create.flatMap(_.info(s"continuing auto scaling for ${l.autoScalingGroupName}")) >>
      autoScalingClient.completeLifecycleAction(
        lifecycleHookName = l.lifecycleHookName.value,
        autoScalingGroupName = l.autoScalingGroupName.value,
        lifecycleActionResult = LifecycleActionResult("CONTINUE"),
        instanceId = l.EC2InstanceId.forAutoScaling.some,
      )
        .void

}
