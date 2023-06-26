package com.dwolla.autoscaling.ecs.draining

import cats.*
import cats.effect.*
import cats.syntax.all.*
import com.amazonaws.ecs.ECS
import com.dwolla.aws.autoscaling.AutoScalingAlg
import com.dwolla.aws.autoscaling.model.LifecycleHookNotification
import com.dwolla.aws.ecs.EcsAlg
import com.dwolla.aws.sns.model.SnsTopicArn
import feral.lambda.events.SnsEvent
import feral.lambda.{INothing, IOLambda, LambdaEnv}
import fs2.Stream
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
    } yield TerminationEventHandler[IO](bridgeFunction)
}

object TerminationEventHandler {
  def apply[F[_] : MonadThrow : LoggerFactory](terminationEventBridge: (SnsTopicArn, LifecycleHookNotification) => F[Unit])
                                              (implicit C: fs2.Compiler[F, F]): LambdaEnv[F, SnsEvent] => F[Option[INothing]] = env =>
    Stream.eval(env.event)
      .map(_.records)
      .flatMap(Stream.emits(_))
      .map(_.sns)
      .evalMap(ParseLifecycleHookNotification[F])
      .unNone
      .evalMap(terminationEventBridge.tupled)
      .compile
      .drain
      .as(None)
}
