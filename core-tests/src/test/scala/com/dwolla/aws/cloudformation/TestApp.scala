package com.dwolla.aws.cloudformation

import cats.effect.*
import cats.syntax.all.*
import com.amazonaws.cloudformation.{CloudFormation, LogicalResourceId}
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import smithy4s.aws.{AwsClient, AwsEnvironment, AwsRegion}

object TestApp extends ResourceApp.Simple {
  given LoggerFactory[IO] = Slf4jFactory.create[IO]

  private def stackArn = StackArn("does-not-exist")
  private val logicalResourceId = LogicalResourceId("bar")

  override def run: Resource[IO, Unit] =
    EmberClientBuilder.default[IO]
      .build
      .flatMap(AwsEnvironment.default(_, AwsRegion.US_WEST_2))
      .flatMap(AwsClient(CloudFormation, _))
      .map(CloudFormationAlg[IO](_))
      .evalMap { alg =>
        alg.physicalResourceIdFor(stackArn, logicalResourceId)
      }
      .evalMap(IO.println)
      .void
}
