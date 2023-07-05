package com.dwolla.aws.autoscaling

import cats.effect.*
import com.dwolla.aws.autoscaling.model.LifecycleHookNotification
import com.dwolla.aws.sns.model.SnsTopicArn

class TestAutoScalingAlg extends AutoScalingAlg[IO] {
  override def pauseAndRecurse(topic: SnsTopicArn, lifecycleHookNotification: LifecycleHookNotification): IO[Unit] = ???
  override def continueAutoScaling(l: LifecycleHookNotification): IO[Unit] = ???
}
