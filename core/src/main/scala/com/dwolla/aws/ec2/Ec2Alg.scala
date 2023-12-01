package com.dwolla.aws.ec2

import cats.effect.*
import cats.syntax.all.*
import com.dwolla.aws.*
import org.typelevel.log4cats.*
import com.amazonaws.ec2.{Tag as _, *}

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
}

extension [A] (maybeList: Option[List[A]]) {
  def collapse: List[A] =
    maybeList.getOrElse(List.empty)
}
