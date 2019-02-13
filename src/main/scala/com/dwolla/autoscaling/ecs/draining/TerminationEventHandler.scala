package com.dwolla.autoscaling.ecs.draining

import cats.effect._
import com.amazonaws.services.autoscaling.AmazonAutoScalingAsync
import com.amazonaws.services.ecs.AmazonECSAsync
import com.amazonaws.services.ecs.model.{ContainerInstance => _, Resource => _}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.AmazonSNSAsync
import com.dwolla.aws.autoscaling.AutoScalingAlg
import com.dwolla.aws.autoscaling.model.LifecycleHookNotification
import com.dwolla.aws.ecs.EcsAlg
import com.dwolla.aws.{autoscaling, ecs, sns}
import com.dwolla.aws.lambda.fs2.LambdaStreamApp
import fs2._

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

class TerminationEventHandler(ecsClientResource: Resource[IO, AmazonECSAsync],
                              autoScalingClientResource: Resource[IO, AmazonAutoScalingAsync],
                              snsClientResource: Resource[IO, AmazonSNSAsync],
                              bridgeFactory: (EcsAlg[IO, Stream[IO, ?]], AutoScalingAlg[IO]) => LifecycleHookNotification => IO[Unit],
                             )(implicit contextShift: ContextShift[IO], timer: Timer[IO]) extends LambdaStreamApp[IO] {
  def this() = this(
    ecs.resource,
    autoscaling.resource,
    sns.resource,
    TerminationEventBridge.apply,
  )(IO.contextShift(global), IO.timer(global))

  override def run(context: Context,
                   blockingExecutionContext: ExecutionContext)
                  (stream: Stream[IO, Byte]): Stream[IO, Byte] =
    (for {
      lifecycleHook <- stream.through(ParseLifecycleHookNotification[IO])
      ecsClient <- Stream.resource(ecsClientResource)
      autoScalingClient <- Stream.resource(autoScalingClientResource)
      snsClient <- Stream.resource(snsClientResource)
      ecsInterpreter = EcsAlg[IO](ecsClient)
      autoScalingInterpreter = AutoScalingAlg[IO](autoScalingClient, snsClient)
      bridge = bridgeFactory(ecsInterpreter, autoScalingInterpreter)
      _ <- Stream.eval(bridge(lifecycleHook))
    } yield ()).drain

}
