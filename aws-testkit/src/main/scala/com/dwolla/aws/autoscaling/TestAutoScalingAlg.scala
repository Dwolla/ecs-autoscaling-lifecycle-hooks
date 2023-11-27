package com.dwolla.aws.autoscaling

import cats.effect.*
import com.amazonaws.sns.TopicARN

abstract class TestAutoScalingAlg extends AutoScalingAlg[IO] {
  override def pauseAndRecurse(topic: TopicARN, lifecycleHookNotification: LifecycleHookNotification, onlyIfInState: LifecycleState): IO[Unit] = IO.raiseError(new NotImplementedError)
  override def continueAutoScaling(l: LifecycleHookNotification): IO[Unit] = IO.raiseError(new NotImplementedError)
}
