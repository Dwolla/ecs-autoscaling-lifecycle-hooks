package com.dwolla.aws.cloudformation

import cats.effect.*
import cats.syntax.all.*
import com.amazonaws.cloudformation.*
import monix.newtypes.NewtypeWrapped
import org.typelevel.log4cats.*
import smithy4s.http.UnknownErrorResponse

type StackArn = StackArn.Type
object StackArn extends NewtypeWrapped[String]

trait CloudFormationAlg[F[_]] {
  def physicalResourceIdFor(stack: StackArn, logicalResourceId: LogicalResourceId): F[Option[PhysicalResourceId]]
}

object CloudFormationAlg {
  def apply[F[_] : Async : LoggerFactory](client: CloudFormation[F]): CloudFormationAlg[F] = new CloudFormationAlg[F] {
    override def physicalResourceIdFor(stack: StackArn, logicalResourceId: LogicalResourceId): F[Option[PhysicalResourceId]] =
      LoggerFactory[F].create.flatMap { case given Logger[F] =>
        Logger[F].info(s"retrieving $logicalResourceId from $stack") >>
          client.describeStackResources(StackName(stack.value).some, logicalResourceId.some)
            .map(_.stackResources.flatMap(_.collectFirstSome(_.physicalResourceId)))
            .recoverWith {
              case ex: UnknownErrorResponse if ex.getMessage.contains(s"Stack with id ${stack.value} does not exist") =>
                Logger[F]
                  .trace(ex)(s"Could not find stack $stack")
                  .as(None)
            }
      }
  }
}
