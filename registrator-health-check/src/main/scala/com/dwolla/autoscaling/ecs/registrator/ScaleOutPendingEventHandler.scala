package com.dwolla.autoscaling.ecs.registrator

import cats.*
import cats.effect.*
import com.amazonaws.ecs.ECS
import com.dwolla.aws.autoscaling.{AutoScalingAlg, LifecycleHookHandler}
import com.dwolla.aws.ec2.Ec2Alg
import com.dwolla.aws.cloudformation.CloudFormationAlg
import com.dwolla.aws.ecs.EcsAlg
import com.dwolla.aws.sns.SnsAlg
import feral.lambda.events.SnsEvent
import feral.lambda.{INothing, IOLambda, LambdaEnv}
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import smithy4s.aws.http4s.*
import smithy4s.aws.kernel.AwsRegion
import software.amazon.awssdk.services.autoscaling.AutoScalingAsyncClient
import software.amazon.awssdk.services.cloudformation.CloudFormationAsyncClient
import software.amazon.awssdk.services.ec2.Ec2AsyncClient
import software.amazon.awssdk.services.sns.SnsAsyncClient

class ScaleOutPendingEventHandler extends IOLambda[SnsEvent, INothing] {
  override def handler: Resource[IO, LambdaEnv[IO, SnsEvent] => IO[Option[INothing]]] =
    for {
      client <- EmberClientBuilder.default[IO].build
      given LoggerFactory[IO] = Slf4jFactory.create[IO]
      ecs <- ECS.simpleAwsClient(client, AwsRegion.US_WEST_2).map(EcsAlg(_))
      autoscalingClient <- Resource.fromAutoCloseable(IO(AutoScalingAsyncClient.builder().build()))
      sns <- Resource.fromAutoCloseable(IO(SnsAsyncClient.builder().build())).map(SnsAlg[IO](_))
      ec2Client <- Resource.fromAutoCloseable(IO(Ec2AsyncClient.builder().build())).map(Ec2Alg[IO](_))
      cloudformationClient <- Resource.fromAutoCloseable(IO(CloudFormationAsyncClient.builder().build())).map(CloudFormationAlg[IO](_))
      autoscaling = AutoScalingAlg[IO](autoscalingClient, sns)
      bridgeFunction = ScaleOutPendingEventBridge(ecs, autoscaling, ec2Client, cloudformationClient)
    } yield LifecycleHookHandler[IO](bridgeFunction)
}
