package com.dwolla.aws.ecs.model

import java.util.UUID

import cats.implicits._
import com.amazonaws.regions.Regions
import com.dwolla.aws.AccountId
import com.dwolla.aws.ArbitraryInstances._
import com.dwolla.aws.ec2.model.Ec2InstanceId
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.ScalacheckShapeless._
import org.scalacheck._

object ArbitraryInstances extends ArbitraryInstances

trait ArbitraryInstances {

  type ArbitraryCluster = List[ClusterWithInstances]
  type ClusterWithInstances = (Cluster, List[ContainerInstance])

  implicit val arbContainerInstance: Arbitrary[ContainerInstance] =
    Arbitrary(for {
      cId <- arbitrary[ContainerInstanceId]
      ec2Id <- arbitrary[Ec2InstanceId]
      taskCount <- arbitrary[TaskCount]
      status <- arbitrary[ContainerStatus]
    } yield ContainerInstance(cId, ec2Id, taskCount, status))

  implicit def arbClusterWithInstances: Arbitrary[ClusterWithInstances] =
    Arbitrary(for {
      cluster <- arbitrary[Cluster]
      instances <- Gen.containerOf[List, ContainerInstance](arbContainerInstance.arbitrary).map(_.take(10))
    } yield cluster -> instances)

  implicit val arbClustersWithInstances: Arbitrary[ArbitraryCluster] =
    Arbitrary {
      Gen.nonEmptyContainerOf[List, ClusterWithInstances](arbitrary[ClusterWithInstances])
        .suchThat { f: List[(Cluster, List[ContainerInstance])] =>
          val containerInstanceCount = f.unorderedFoldMap(_._2.size)
          val containerInstanceIdsAreUnique = f.flatMap(_._2.map(_.containerInstanceId)).toSet.size == containerInstanceCount
          val ec2InstanceIdsAreUnique = f.flatMap(_._2.map(_.ec2InstanceId)).toSet.size == containerInstanceCount

          containerInstanceIdsAreUnique && ec2InstanceIdsAreUnique && containerInstanceCount > 0
        }
        .map(_.take(10))
    }

  implicit val arbContainerInstanceId: Arbitrary[ContainerInstanceId] =
    Arbitrary(arbitrary[UUID].map(_.toString).map(tagContainerInstanceId))

  implicit val arbCluster: Arbitrary[Cluster] =
    Arbitrary(for {
      region <- arbitrary[Regions]
      accountId <- arbitrary[AccountId]
      clusterName <- arbitrary[ClusterName]
    } yield Cluster(region, accountId, clusterName))

  implicit val arbClusterArn: Arbitrary[ClusterArn] = Arbitrary(arbitrary[Cluster].map(_.clusterArn))

  implicit val arbClusterName: Arbitrary[ClusterName] = Arbitrary(Gen.oneOf("cluster1", "cluster2", "cluster3").map(tagClusterName))

  implicit val arbTaskCount: Arbitrary[TaskCount] = Arbitrary(Gen.chooseNum(0, 20).map(tagTaskCount))
}
