package com.dwolla.autoscaling.ecs.draining

import cats._
import cats.implicits._
import com.dwolla.aws.autoscaling.AutoScalingAlg
import com.dwolla.aws.autoscaling.model.LifecycleHookNotification
import com.dwolla.aws.ecs.EcsAlg

class TerminationEventBridge[F[_] : Monad, G[_]](ECS: EcsAlg[F, G], AutoScaling: AutoScalingAlg[F]) {
  private val F = Applicative[F]

  def apply(lifecycleHook: LifecycleHookNotification): F[Unit] =
    for {
      maybeInstance <- ECS.findEc2Instance(lifecycleHook.EC2InstanceId)
      tasksRemaining <- maybeInstance match {
        case Some((cluster, ci)) =>
          ECS.drainInstance(cluster, ci) >> F.pure(ci.runningTaskCount > 0)
        case None => F.pure(false)
      }
      _ <- if (tasksRemaining) AutoScaling.pauseAndRecurse else AutoScaling.continueAutoScaling(lifecycleHook)
    } yield ()
}

object TerminationEventBridge {
  def apply[F[_] : Monad, G[_]](ecsAlg: EcsAlg[F, G], autoScalingAlg: AutoScalingAlg[F]): LifecycleHookNotification => F[Unit] =
    new TerminationEventBridge(ecsAlg, autoScalingAlg).apply
}
