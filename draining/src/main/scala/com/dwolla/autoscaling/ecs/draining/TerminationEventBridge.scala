package com.dwolla.autoscaling.ecs.draining

import cats.*
import cats.syntax.all.*
import com.dwolla.aws.autoscaling.*
import com.dwolla.aws.ecs.*
import com.dwolla.aws.sns.SnsTopicArn

class TerminationEventBridge[F[_] : Monad, G[_]](ECS: EcsAlg[F, G], AutoScaling: AutoScalingAlg[F]) {
  def apply(topic: SnsTopicArn, lifecycleHook: LifecycleHookNotification): F[Unit] =
    for {
      maybeInstance <- ECS.findEc2Instance(lifecycleHook.EC2InstanceId)
      tasksRemaining <- maybeInstance match {
        case Some((cluster, ci)) =>
          ECS.drainInstance(cluster, ci).as(ci.runningTaskCount > TaskCount(0))
        case None => false.pure[F]
      }
      _ <- if (tasksRemaining) AutoScaling.pauseAndRecurse(topic, lifecycleHook) else AutoScaling.continueAutoScaling(lifecycleHook)
    } yield ()
}

object TerminationEventBridge {
  def apply[F[_] : Monad, G[_]](ecsAlg: EcsAlg[F, G], autoScalingAlg: AutoScalingAlg[F]): (SnsTopicArn, LifecycleHookNotification) => F[Unit] =
    new TerminationEventBridge(ecsAlg, autoScalingAlg).apply
}
