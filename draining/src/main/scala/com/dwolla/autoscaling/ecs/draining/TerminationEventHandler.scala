package com.dwolla.autoscaling.ecs.draining

import cats.*
import cats.effect.*
import com.amazonaws.ecs.ECS
import com.dwolla.aws.autoscaling.{AutoScalingAlg, LifecycleHookHandler}
import com.dwolla.aws.ecs.EcsAlg
import feral.lambda.events.SnsEvent
import feral.lambda.{INothing, IOLambda, LambdaEnv}
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import smithy4s.aws.http4s.*
import smithy4s.aws.kernel.AwsRegion
import software.amazon.awssdk.services.autoscaling.AutoScalingAsyncClient
import software.amazon.awssdk.services.sns.SnsAsyncClient

class TerminationEventHandler extends IOLambda[SnsEvent, INothing] {
  override def handler: Resource[IO, LambdaEnv[IO, SnsEvent] => IO[Option[INothing]]] =
    for {
      client <- EmberClientBuilder.default[IO].build
      given LoggerFactory[IO] = Slf4jFactory.create[IO]
      ecs <- ECS.simpleAwsClient(client, AwsRegion.US_WEST_2).map(EcsAlg(_))
      autoscalingClient <- Resource.fromAutoCloseable(IO(AutoScalingAsyncClient.builder().build()))
      snsClient <- Resource.fromAutoCloseable(IO(SnsAsyncClient.builder().build()))
      autoscaling = AutoScalingAlg[IO](autoscalingClient, snsClient)
      bridgeFunction = TerminationEventBridge(ecs, autoscaling)
    } yield LifecycleHookHandler[IO](bridgeFunction)
}
