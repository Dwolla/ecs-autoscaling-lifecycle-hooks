package com.dwolla.aws.ecs

import cats.*
import cats.syntax.all.*
import com.amazonaws.ecs.{ContainerInstanceStatus, ECS}
import com.dwolla.aws.ec2.model.*
import com.dwolla.aws.ecs.model.*
import com.dwolla.fs2utils.Pagination
import fs2.*
import org.typelevel.log4cats.{Logger, LoggerFactory}

abstract class EcsAlg[F[_] : Applicative, G[_]] {
  def listClusterArns: G[ClusterArn]
  def listContainerInstances(cluster: ClusterArn): G[ContainerInstance]
  def findEc2Instance(ec2InstanceId: Ec2InstanceId): F[Option[(ClusterArn, ContainerInstance)]]
  def drainInstance(cluster: ClusterArn, ci: ContainerInstance): F[Unit] =
    Applicative[F].unlessA(ci.status == ContainerStatus.Draining)(drainInstanceImpl(cluster, ci))

  protected def drainInstanceImpl(cluster: ClusterArn, ci: ContainerInstance): F[Unit]
}

object EcsAlg {
  def apply[F[_] : Monad : LoggerFactory](ecs: ECS[F])
                                         (implicit C: Compiler[F, F]): EcsAlg[F, Stream[F, *]] = new EcsAlg[F, Stream[F, *]] {
    override def listClusterArns: Stream[F, ClusterArn] =
      Pagination.offsetUnfoldChunkEval {
        ecs
          .listClusters(_: Option[String], None)
          .map { resp =>
            resp.clusterArns.map(Chunk.iterable(_)).getOrElse(Chunk.empty) -> resp.nextToken
          }
      }
        .map(ClusterArn(_))

    override def listContainerInstances(cluster: ClusterArn): Stream[F, ContainerInstance] =
      Pagination.offsetUnfoldChunkEval { (nextToken: Option[String]) =>
        ecs
          .listContainerInstances(cluster.value.some, nextToken = nextToken)
          .map { resp =>
            resp.containerInstanceArns.map(Chunk.iterable(_)).getOrElse(Chunk.empty) -> resp.nextToken
          }
      }
        .chunkN(100)
        .map(_.toList)
        .evalMap(ecs.describeContainerInstances(_, cluster = cluster.value.some))
        .map(_.containerInstances.map(Chunk.iterable(_)).getOrElse(Chunk.empty))
        .unchunks
        .map { ci =>
          (ci.containerInstanceArn.map(ContainerInstanceId(_)), ci.ec2InstanceId.map(Ec2InstanceId(_)), ci.status.flatMap(ContainerStatus.fromStatus))
            .mapN(ContainerInstance(_, _, TaskCount(ci.runningTasksCount), _))
        }
        .unNone

    override def findEc2Instance(ec2InstanceId: Ec2InstanceId): F[Option[(ClusterArn, ContainerInstance)]] =
      LoggerFactory[F].create.flatMap { implicit L =>
        listClusterArns
          .mproduct(listContainerInstances(_).filter(_.ec2InstanceId == ec2InstanceId))
          .compile
          .last
          .flatTap { ec2Instance =>
            Logger[F].info(s"EC2 Instance search results: $ec2Instance")
          }
      }

    override protected def drainInstanceImpl(cluster: ClusterArn, ci: ContainerInstance): F[Unit] =
      LoggerFactory[F].create.flatMap { implicit L =>
        Logger[F].info(s"draining instance $ci in cluster $cluster") >>
          ecs.updateContainerInstancesState(List(ci.containerInstanceId.value), ContainerInstanceStatus.DRAINING, cluster.value.some)
            .void
      }
  }
}
