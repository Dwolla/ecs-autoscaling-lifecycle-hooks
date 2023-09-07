package com.dwolla.aws.ecs

import cats.*
import cats.syntax.all.*
import com.amazonaws.ecs.ECS
import com.dwolla.aws.ec2.*
import com.dwolla.aws.ecs.*
import com.dwolla.aws.ecs.TaskStatus.{Running, stoppedTaskStatuses}
import com.dwolla.fs2utils.Pagination
import fs2.*
import monix.newtypes.HasExtractor
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
                                         (using Compiler[F, F]): EcsAlg[F, Stream[F, *]] = new EcsAlg[F, Stream[F, *]] {
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
              resp.containerInstanceArns.toChunk.map(ContainerInstanceId(_)) -> resp.nextToken
            }
        }
        .through(chunkedEcsRequest(ecs.describeContainerInstances(_, cluster = cluster.value.some))(_.containerInstances))
        .map { ci =>
          (ci.containerInstanceArn.map(ContainerInstanceId(_)),
            ci.ec2InstanceId.map(Ec2InstanceId(_)),
            ci.status.flatMap(ContainerInstanceStatus.fromStatus),
          )
            .tupled
        }
        .unNone
        .evalMap { (ci, ec2, status) =>
          listTasks(cluster, ci)
            .through(chunkedEcsRequest(ecs.describeTasks(_, cluster.value.some))(_.tasks))
            .filterNot(_.lastStatus.flatMap(TaskStatus.fromString.lift).exists(stoppedTaskStatuses.contains))
            .compile
            .count
            .map(TaskCount(_))
            .map(ContainerInstance(ci, ec2, _, status))
        }

    /**
     * Many ECS Describe* APIs accept up to 100 identifiers to be described in a single request.
     * This helper function generically chunks the requests accordingly.
     */
    private def chunkedEcsRequest[A, B, C, AA](f: List[A] => F[B])
                                              (extract: B => Option[List[C]])
                                              (using HasExtractor.Aux[AA, A]): Pipe[F, AA, C] =
      _.chunkN(100)
        .map(_.toList)
        .evalMap(f.compose(_.map(implicitly[HasExtractor.Aux[AA, A]].extract)))
        .map(extract(_).toChunk)
        .unchunks

    override def findEc2Instance(ec2InstanceId: Ec2InstanceId): F[Option[(ClusterArn, ContainerInstance)]] =
      LoggerFactory[F].create.flatMap { case given Logger[F] =>
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
                          ci: ContainerInstanceId): Stream[F, TaskArn] =
      for {
        given _ <- Stream.eval(LoggerFactory[F].create)
        _ <- Stream.eval(Logger[F].info(s"listing tasks on instance ${ci.value} in cluster $cluster"))
        taskArn <- Pagination.offsetUnfoldChunkEval { (nextToken: Option[String]) =>
          for {
            _ <- Logger[F].trace(s"cluster = ${cluster.value}, containerInstance = ${ci.value}, nextToken = $nextToken")
            resp <- ecs.listTasks(
              cluster = cluster.value.some,
              containerInstance = ci.value.some,
              nextToken = nextToken,
            )
          } yield resp.taskArns.map(Chunk.iterable(_)).getOrElse(Chunk.empty) -> resp.nextToken
        }
      } yield TaskArn(taskArn)

    override def isTaskDefinitionRunningOnInstance(cluster: ClusterArn,
                                                   ci: ContainerInstance,
                                                   taskDefinition: TaskDefinitionArn): F[Boolean] =
      LoggerFactory[F].create.flatMap { case given Logger[F] =>
        Logger[F].info(s"looking for task definition ${taskDefinition.value} on instance ${ci.containerInstanceId.value} in cluster ${cluster.value}") >>
          listTasks(cluster, ci.containerInstanceId)
            .through(chunkedEcsRequest(ecs.describeTasks(_, cluster.value.some))(_.tasks))
            .filter(_.taskDefinitionArn.map(TaskDefinitionArn(_)).contains(taskDefinition))
            .filter(_.lastStatus.flatMap(TaskStatus.fromString.lift).contains(Running))
            .head
            .compile
            .last
            .map(_.isDefined)
      }

    override protected def drainInstanceImpl(cluster: ClusterArn, ci: ContainerInstance): F[Unit] =
      LoggerFactory[F].create.flatMap { case given Logger[F] =>
        Logger[F].info(s"draining instance $ci in cluster $cluster") >>
          ecs.updateContainerInstancesState(List(ci.containerInstanceId.value), com.amazonaws.ecs.ContainerInstanceStatus.DRAINING, cluster.value.some)
            .void
      }
  }

  extension[A] (maybeList: Option[List[A]]) {
    def toChunk: Chunk[A] =
      maybeList.fold(Chunk.empty)(Chunk.iterable)
  }
}
