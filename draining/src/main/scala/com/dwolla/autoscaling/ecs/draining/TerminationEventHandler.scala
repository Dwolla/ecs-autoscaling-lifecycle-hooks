package com.dwolla.autoscaling.ecs.draining

import cats.*
import cats.effect.*
import com.amazonaws.autoscaling.AutoScaling
import com.amazonaws.ecs.ECS
import com.amazonaws.sns.SNS
import com.dwolla.aws.autoscaling.{AutoScalingAlg, LifecycleHookHandler}
import com.dwolla.aws.ecs.EcsAlg
import com.dwolla.aws.sns.SnsAlg
import feral.lambda.events.SnsEvent
import feral.lambda.{INothing, IOLambda, LambdaEnv}
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import smithy4s.aws.*
import smithy4s.aws.kernel.AwsRegion

class TerminationEventHandler extends IOLambda[SnsEvent, INothing] {
  override def handler: Resource[IO, LambdaEnv[IO, SnsEvent] => IO[Option[INothing]]] =
    for {
      client <- EmberClientBuilder.default[IO].build
      given LoggerFactory[IO] = Slf4jFactory.create[IO]
      awsEnv <- AwsEnvironment.default(client, AwsRegion.US_WEST_2)
      ecs <- AwsClient(ECS, awsEnv).map(EcsAlg(_))
      autoscalingClient <- AwsClient(AutoScaling, awsEnv)
      sns <- AwsClient(SNS, awsEnv).map(SnsAlg[IO](_))
      autoscaling = AutoScalingAlg[IO](autoscalingClient, sns)
      bridgeFunction = TerminationEventBridge(ecs, autoscaling)
    } yield LifecycleHookHandler[IO](bridgeFunction)
}
