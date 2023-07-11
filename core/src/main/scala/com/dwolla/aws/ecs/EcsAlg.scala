package com.dwolla.aws.ecs

import cats.*
import cats.syntax.all.*
import com.amazonaws.ecs.{ECS, Task}
import com.dwolla.aws.ec2.*
import com.dwolla.aws.ecs.*
import com.dwolla.aws.ecs.TaskStatus.Running
import com.dwolla.fs2utils.Pagination
import fs2.*
import org.typelevel.log4cats.{Logger, LoggerFactory}

abstract class EcsAlg[F[_] : Applicative, G[_]] {
  def listClusterArns: G[ClusterArn]
  def listContainerInstances(cluster: ClusterArn): G[ContainerInstance]
  def findEc2Instance(ec2InstanceId: Ec2InstanceId): F[Option[(ClusterArn, ContainerInstance)]]
  def drainInstance(cluster: ClusterArn, ci: ContainerInstance): F[Unit] =
    drainInstanceImpl(cluster, ci).unlessA(ci.status == ContainerInstanceStatus.Draining)

  def isTaskDefinitionRunningOnInstance(cluster: ClusterArn,
                                        ci: ContainerInstance,
                                        taskDefinition: TaskDefinitionArn): F[Boolean]

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
          (ci.containerInstanceArn.map(ContainerInstanceId(_)), ci.ec2InstanceId.map(Ec2InstanceId(_)), ci.status.flatMap(ContainerInstanceStatus.fromStatus))
            .mapN(ContainerInstance(_, _, TaskCount(ci.runningTasksCount), _))
        }
        .unNone

    override def findEc2Instance(ec2InstanceId: Ec2InstanceId): F[Option[(ClusterArn, ContainerInstance)]] =
      LoggerFactory[F].create.flatMap { implicit L =>
        listClusterArns
          // TODO listContainerInstances could use a CQL expression to narrow the search
          .mproduct(listContainerInstances(_).filter(_.ec2InstanceId == ec2InstanceId))
          .compile
          .last
          .flatTap { ec2Instance =>
            Logger[F].info(s"EC2 Instance search results: $ec2Instance")
          }
      }

    private def listTasks(cluster: ClusterArn,
                          ci: ContainerInstance): Stream[F, TaskArn] =
      for {
        given _ <- Stream.eval(LoggerFactory[F].create)
        _ <- Stream.eval(Logger[F].info(s"listing tasks on instance ${ci.containerInstanceId.value} in cluster $cluster"))
        taskArn <- Pagination.offsetUnfoldChunkEval { (nextToken: Option[String]) =>
          for {
            _ <- Logger[F].trace(s"cluster = ${cluster.value}, containerInstance = ${ci.containerInstanceId.value}, nextToken = $nextToken")
            resp <- ecs.listTasks(
              cluster = cluster.value.some,
              containerInstance = ci.containerInstanceId.value.some,
              nextToken = nextToken,
            )
          } yield resp.taskArns.map(Chunk.iterable(_)).getOrElse(Chunk.empty) -> resp.nextToken
        }
      } yield TaskArn(taskArn)

    private def describeTasks(cluster: ClusterArn): Pipe[F, TaskArn, Task] =
      _.map(_.value)
        .chunkN(100)
        .map(_.toList)
        .evalMap(ecs.describeTasks(_, cluster.value.some))
        .map(_.tasks.map(Chunk.iterable(_)).getOrElse(Chunk.empty))
        .unchunks

    override def isTaskDefinitionRunningOnInstance(cluster: ClusterArn,
                                                   ci: ContainerInstance,
                                                   taskDefinition: TaskDefinitionArn): F[Boolean] =
      LoggerFactory[F].create.flatMap { implicit L =>
        Logger[F].info(s"looking for task definition ${taskDefinition.value} on instance ${ci.containerInstanceId.value} in cluster ${cluster.value}") >>
          listTasks(cluster, ci)
            .through(describeTasks(cluster))
            .filter(_.taskDefinitionArn.map(TaskDefinitionArn(_)).contains(taskDefinition))
            .filter(_.lastStatus.flatMap(TaskStatus.fromString).contains(Running))
            .head
            .compile
            .last
            .map(_.isDefined)
      }

    override protected def drainInstanceImpl(cluster: ClusterArn, ci: ContainerInstance): F[Unit] =
      LoggerFactory[F].create.flatMap { implicit L =>
        Logger[F].info(s"draining instance $ci in cluster $cluster") >>
          ecs.updateContainerInstancesState(List(ci.containerInstanceId.value), com.amazonaws.ecs.ContainerInstanceStatus.DRAINING, cluster.value.some)
            .void
      }
  }
}
