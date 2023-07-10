package com.dwolla.aws.ecs

import cats.effect.*
import com.dwolla.aws.ec2.*
import com.dwolla.aws.ecs.*

abstract class TestEcsAlg extends EcsAlg[IO, List] {
  override def listClusterArns: List[ClusterArn] = ???
  override def listContainerInstances(cluster: ClusterArn): List[ContainerInstance] = ???
  override def findEc2Instance(ec2InstanceId: Ec2InstanceId): IO[Option[(ClusterArn, ContainerInstance)]] = ???
  override def drainInstanceImpl(cluster: ClusterArn, ci: ContainerInstance): IO[Unit] = ???
}
