package com.dwolla.aws.cloudformation

import cats.effect.*
import cats.syntax.all.*
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.noop.NoOpFactory
import software.amazon.awssdk.services.cloudformation.CloudFormationAsyncClient

object TestApp extends ResourceApp.Simple {
  private implicit val loggerFactory: LoggerFactory[IO] = NoOpFactory[IO]

  private def stackArn = StackArn(???)
  private val logicalResourceId = LogicalResourceId(???)

  override def run: Resource[IO, Unit] =
    Resource.fromAutoCloseable(IO(CloudFormationAsyncClient.builder().build()))
      .map(CloudFormationAlg[IO](_))
      .evalMap { alg =>
        alg.physicalResourceIdFor(stackArn, logicalResourceId)
      }
      .evalMap(IO.println)
      .void
}
