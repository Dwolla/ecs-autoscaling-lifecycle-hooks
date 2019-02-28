package com.dwolla.autoscaling.ecs.draining

import cats._
import cats.effect._
import com.amazonaws.services.autoscaling.AmazonAutoScalingAsync
import com.amazonaws.services.ecs.AmazonECSAsync
import com.amazonaws.services.ecs.model.{ContainerInstance => _, Resource => _}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.AmazonSNSAsync
import com.dwolla.aws.autoscaling.AutoScalingAlg
import com.dwolla.aws.autoscaling.model.LifecycleHookNotification
import com.dwolla.aws.ecs.EcsAlg
import com.dwolla.aws.lambda.fs2.LambdaStreamApp
import com.dwolla.aws.sns.model.SnsTopicArn
import com.dwolla.aws.{autoscaling, ecs, sns}
import io.chrisdavenport.log4cats._
import fs2._

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

class TerminationEventHandler(ecsClientResource: Resource[IO, AmazonECSAsync],
                              autoScalingClientResource: Resource[IO, AmazonAutoScalingAsync],
                              snsClientResource: Resource[IO, AmazonSNSAsync],
                              bridgeFunction: (EcsAlg[IO, Stream[IO, ?]], AutoScalingAlg[IO]) => (SnsTopicArn, LifecycleHookNotification) => IO[Unit],
                             )(implicit contextShift: ContextShift[IO], timer: Timer[IO], L: Logger[IO]) extends LambdaStreamApp[IO] {
  def this() = this(
    ecs.resource,
    autoscaling.resource,
    sns.resource,
    TerminationEventBridge.apply(_, _)(Monad[IO], com.dwolla.aws.lambda.fs2.logger[IO]),
  )(IO.contextShift(global), IO.timer(global), com.dwolla.aws.lambda.fs2.logger[IO])

  private def createResourcesAndProcess(stream: Stream[IO, Byte]): Stream[IO, Unit] =
    for {
      (topicArn, lifecycleHook) <- stream.through(ParseLifecycleHookNotification[IO])
      ecsClient <- Stream.resource(ecsClientResource)
      autoScalingClient <- Stream.resource(autoScalingClientResource)
      snsClient <- Stream.resource(snsClientResource)
      ecsInterpreter = EcsAlg[IO](ecsClient)
      autoScalingInterpreter = AutoScalingAlg[IO](autoScalingClient, snsClient)
      bridge = bridgeFunction(ecsInterpreter, autoScalingInterpreter)
      _ <- Stream.eval(bridge(topicArn, lifecycleHook))
    } yield ()

  override def run(context: Context,
                   blockingExecutionContext: ExecutionContext)
                  (stream: Stream[IO, Byte]): Stream[IO, Byte] =
    createResourcesAndProcess(stream).drain
}
