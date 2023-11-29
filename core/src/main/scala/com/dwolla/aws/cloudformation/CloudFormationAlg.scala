package com.dwolla.aws
package cloudformation

import cats.effect.*
import cats.syntax.all.*
import cats.tagless.aop.Aspect
import cats.~>
import com.amazonaws.cloudformation.*
import com.dwolla.aws.TraceableValueInstances.given
import monix.newtypes.NewtypeWrapped
import natchez.TraceableValue
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

  given Aspect[CloudFormationAlg, TraceableValue, TraceableValue] = new Aspect[CloudFormationAlg, TraceableValue, TraceableValue] {
    override def weave[F[_]](af: CloudFormationAlg[F]): CloudFormationAlg[[A] =>> Aspect.Weave[F, TraceableValue, TraceableValue, A]] =
      new CloudFormationAlg[[A] =>> Aspect.Weave[F, TraceableValue, TraceableValue, A]] {
        override def physicalResourceIdFor(stack: StackArn, logicalResourceId: LogicalResourceId): Aspect.Weave[F, TraceableValue, TraceableValue, Option[PhysicalResourceId]] =
          Aspect.Weave(
            "CloudFormationAlg",
            List(List(
              Aspect.Advice.byValue[TraceableValue, StackArn]("stack", stack),
              Aspect.Advice.byValue[TraceableValue, LogicalResourceId]("logicalResourceId", logicalResourceId),
            )),
            Aspect.Advice[F, TraceableValue, Option[PhysicalResourceId]]("physicalResourceIdFor", af.physicalResourceIdFor(stack, logicalResourceId))
          )
      }

    override def mapK[F[_], G[_]](af: CloudFormationAlg[F])(fk: F ~> G): CloudFormationAlg[G] =
      new CloudFormationAlg[G] {
        override def physicalResourceIdFor(stack: StackArn, logicalResourceId: LogicalResourceId): G[Option[PhysicalResourceId]] =
          fk(af.physicalResourceIdFor(stack, logicalResourceId))
      }
  }
}
