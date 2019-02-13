package com.dwolla.aws.ecs

import cats._
import cats.effect._
import cats.implicits._
import com.amazonaws.services.ecs.AmazonECSAsync
import com.amazonaws.services.ecs.model.ContainerInstanceStatus.DRAINING
import com.amazonaws.services.ecs.model.{ContainerInstance => _, _}
import com.dwolla.aws.ec2.model.{Ec2InstanceId, tagEc2InstanceId}
import com.dwolla.aws.ecs.model._
import com.dwolla.fs2aws._
import fs2._

import scala.collection.JavaConverters._

abstract class EcsAlg[F[_] : Monad, G[_]] {
  def listClusterArns: G[ClusterArn]
  def listContainerInstances(cluster: ClusterArn): G[ContainerInstance]
  def findEc2Instance(ec2InstanceId: Ec2InstanceId): F[Option[(ClusterArn, ContainerInstance)]]
  def drainInstance(cluster: ClusterArn, ci: ContainerInstance): F[Unit] =
    Applicative[F].unlessA(ci.status == ContainerStatus.Draining)(drainInstanceImpl(cluster, ci))

  protected def drainInstanceImpl(cluster: ClusterArn, ci: ContainerInstance): F[Unit]
}

object EcsAlg {
  def apply[F[_] : Effect](ecsClient: AmazonECSAsync): EcsAlg[F, Stream[F, ?]] =
    new EcsAlgImpl[F](ecsClient)
}

class EcsAlgImpl[F[_] : Effect](ecsClient: AmazonECSAsync) extends EcsAlg[F, Stream[F, ?]] {
  override def listClusterArns: Stream[F, ClusterArn] =
    listClustersRequest.fetchAll[F](ecsClient.listClustersAsync)(_.getClusterArns.asScala.map(tagClusterArn))

  override def listContainerInstances(cluster: ClusterArn): Stream[F, ContainerInstance] =
    for {
      containerInstanceArns <- listContainerInstancesRequest(cluster).fetchAll[F](ecsClient.listContainerInstancesAsync)(_.getContainerInstanceArns.asScala.map(tagContainerInstanceId)).chunkN(100, allowFewer = true)
      containerInstanceResult <- Stream.eval(describeContainerInstancesRequest(cluster, containerInstanceArns).executeVia[F](ecsClient.describeContainerInstancesAsync))
      ci <- Stream.emits(containerInstanceResult.getContainerInstances.asScala)
    } yield ContainerInstance(
      tagContainerInstanceId(ci.getContainerInstanceArn),
      tagEc2InstanceId(ci.getEc2InstanceId),
      tagTaskCount(ci.getRunningTasksCount),
      ContainerStatus.fromStatus(ci.getStatus),
    )

  override def findEc2Instance(ec2InstanceId: Ec2InstanceId): F[Option[(ClusterArn, ContainerInstance)]] =
    (for {
      cluster <- listClusterArns
      instance <- listContainerInstances(cluster).filter(_.ec2InstanceId == ec2InstanceId)
    } yield cluster -> instance).compile.last

  override def drainInstanceImpl(cluster: ClusterArn, ci: ContainerInstance): F[Unit] =
    new UpdateContainerInstancesStateRequest()
      .withCluster(cluster)
      .withContainerInstances(ci.containerInstanceId)
      .withStatus(DRAINING)
      .executeVia[F](ecsClient.updateContainerInstancesStateAsync)
      .void

  private def listClustersRequest: () => ListClustersRequest =
    () => new ListClustersRequest()

  private def listContainerInstancesRequest(cluster: ClusterArn): () => ListContainerInstancesRequest =
    () => new ListContainerInstancesRequest().withCluster(cluster)

  private def describeContainerInstancesRequest(cluster: ClusterArn, containerInstanceIds: Chunk[ContainerInstanceId]) =
    new DescribeContainerInstancesRequest().withCluster(cluster).withContainerInstances(containerInstanceIds.toList: _*)

}
