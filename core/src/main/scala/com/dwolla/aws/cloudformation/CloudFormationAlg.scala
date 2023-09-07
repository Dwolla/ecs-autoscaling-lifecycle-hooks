package com.dwolla.aws.cloudformation

import cats.effect.*
import cats.syntax.all.*
import monix.newtypes.NewtypeWrapped
import org.typelevel.log4cats.*
import software.amazon.awssdk.services.cloudformation.CloudFormationAsyncClient
import software.amazon.awssdk.services.cloudformation.model.DescribeStackResourceRequest
import software.amazon.awssdk.services.cloudformation.model.CloudFormationException

type StackArn = StackArn.Type
object StackArn extends NewtypeWrapped[String]

type LogicalResourceId = LogicalResourceId.Type
object LogicalResourceId extends NewtypeWrapped[String]

type PhysicalResourceId = PhysicalResourceId.Type
object PhysicalResourceId extends NewtypeWrapped[String]

trait CloudFormationAlg[F[_]] {
  def physicalResourceIdFor(stack: StackArn, logicalResourceId: LogicalResourceId): F[Option[PhysicalResourceId]]
}

object CloudFormationAlg {
  def apply[F[_] : Async : LoggerFactory](client: CloudFormationAsyncClient): CloudFormationAlg[F] = new CloudFormationAlg[F] {
    override def physicalResourceIdFor(stack: StackArn, logicalResourceId: LogicalResourceId): F[Option[PhysicalResourceId]] =
      LoggerFactory[F].create.flatMap { case given Logger[F] =>
        val req = DescribeStackResourceRequest.builder()
          .stackName(stack.value)
          .logicalResourceId(logicalResourceId.value)
          .build()

        Logger[F].info(s"retrieving $logicalResourceId from $stack") >>
          Async[F]
            .fromCompletableFuture {
              Sync[F].delay(client.describeStackResource(req))
            }
            .map(_.stackResourceDetail().physicalResourceId())
            .map(PhysicalResourceId(_).some)
            .recoverWith {
              case ex: CloudFormationException if ex.getMessage.startsWith(s"Resource $logicalResourceId does not exist for stack") =>
                Logger[F]
                  .trace(ex)(s"Could not find $logicalResourceId in $stack")
                  .as(None)
              case ex: CloudFormationException if ex.getMessage.startsWith(s"Stack '$stack' does not exist") =>
                Logger[F]
                  .trace(ex)(s"Could not find stack $stack")
                  .as(None)
            }
      }
  }
}
