package com.dwolla.aws.autoscaling

import cats.effect._
import com.dwolla.aws.autoscaling.model.LifecycleHookNotification

class TestAutoScalingAlg extends AutoScalingAlg[IO] {
  override def pauseAndRecurse: IO[Unit] = ???
  override def continueAutoScaling(l: LifecycleHookNotification): IO[Unit] = ???
}
