package com.dwolla.aws
package ec2

import cats.effect.*
import cats.syntax.all.*
import cats.tagless.aop.Aspect
import cats.~>
import com.amazonaws.ec2.{Tag as _, *}
import com.dwolla.aws.*
import com.dwolla.aws.TraceableValueInstances.given
import natchez.TraceableValue
import org.typelevel.log4cats.*

trait Ec2Alg[F[_]] {
  def getTagsForInstance(id: InstanceId): F[List[Tag]]
}

object Ec2Alg {
  def apply[F[_]: Async : LoggerFactory](ec2Client: EC2[F]): Ec2Alg[F] = new Ec2Alg[F] {
    override def getTagsForInstance(id: InstanceId): F[List[Tag]] =
      LoggerFactory[F].create.flatMap { case given Logger[F] =>
        for {
          _ <- Logger[F].info(s"describing instance ${id.value}")
          resp: DescribeInstancesResult <- ec2Client.describeInstances(instanceIds = id.pure[List].some)
        } yield {
          resp.reservations
            .collapse
            .flatMap(_.instances.collapse)
            .flatMap(_.tags.collapse)
            .map { t =>
              (t.key.map(TagName(_)), t.value.map(TagValue(_))).mapN(Tag.apply)
            }
            .flattenOption
        }
      }
  }

  given Aspect[Ec2Alg, TraceableValue, TraceableValue] = new Aspect[Ec2Alg, TraceableValue, TraceableValue] {
    override def weave[F[_]](af: Ec2Alg[F]): Ec2Alg[[A] =>> Aspect.Weave[F, TraceableValue, TraceableValue, A]] =
      new Ec2Alg[[A] =>> Aspect.Weave[F, TraceableValue, TraceableValue, A]] {
        override def getTagsForInstance(id: InstanceId): Aspect.Weave[F, TraceableValue, TraceableValue, List[Tag]] =
          Aspect.Weave[F, TraceableValue, TraceableValue, List[Tag]](
            "Ec2Alg",
            List(List(
              Aspect.Advice.byValue[TraceableValue, InstanceId]("id", id),
            )),
            Aspect.Advice[F, TraceableValue, List[Tag]]("getTagsForInstance", af.getTagsForInstance(id))
          )
      }

    override def mapK[F[_], G[_]](af: Ec2Alg[F])(fk: F ~> G): Ec2Alg[G] =
      new Ec2Alg[G] {
        override def getTagsForInstance(id: InstanceId): G[List[Tag]] = fk(af.getTagsForInstance(id))
      }
  }
}

extension [A] (maybeList: Option[List[A]]) {
  def collapse: List[A] =
    maybeList.getOrElse(List.empty)
}
