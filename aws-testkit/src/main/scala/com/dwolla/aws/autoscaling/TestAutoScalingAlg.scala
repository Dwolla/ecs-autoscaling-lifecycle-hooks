package com.dwolla.aws.autoscaling

import cats.effect.*
import com.dwolla.aws.sns.SnsTopicArn

abstract class TestAutoScalingAlg extends AutoScalingAlg[IO] {
  override def pauseAndRecurse(topic: SnsTopicArn, lifecycleHookNotification: LifecycleHookNotification, onlyIfInState: LifecycleState): IO[Unit] = IO.raiseError(new NotImplementedError)
  override def continueAutoScaling(l: LifecycleHookNotification): IO[Unit] = IO.raiseError(new NotImplementedError)
}
