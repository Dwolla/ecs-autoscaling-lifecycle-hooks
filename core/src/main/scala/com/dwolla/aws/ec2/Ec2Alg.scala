package com.dwolla.aws.ec2

import cats.effect.*
import cats.syntax.all.*
import com.dwolla.aws.*
import monix.newtypes.NewtypeWrapped
import org.typelevel.log4cats.*
import software.amazon.awssdk.services.ec2.*
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest

import scala.jdk.CollectionConverters.*

type Ec2InstanceId = Ec2InstanceId.Type
object Ec2InstanceId extends NewtypeWrapped[String]

trait Ec2Alg[F[_]] {
  def getTagsForInstance(id: Ec2InstanceId): F[List[Tag]]
}

object Ec2Alg {
  def apply[F[_]: Async : LoggerFactory](ec2Client: Ec2AsyncClient): Ec2Alg[F] = new Ec2Alg[F] {
    override def getTagsForInstance(id: Ec2InstanceId): F[List[Tag]] = {
      val req = DescribeInstancesRequest.builder().instanceIds(id.value).build()

      LoggerFactory[F].create.flatMap { implicit L =>
        for {
          _ <- Logger[F].info(s"describing instance ${id.value}")
          resp <- Async[F].fromCompletableFuture(
            Sync[F].delay(ec2Client.describeInstances(req))
          )
        } yield {
          resp
            .reservations()
            .asScala
            .flatMap(_.instances().asScala)
            .flatMap(_.tags().asScala)
            .map { t =>
              Tag(TagName(t.key()), TagValue(t.value()))
            }
            .toList
        }
      }
    }
  }
}
