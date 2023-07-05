package com.dwolla.aws.ecs.model

import cats.implicits.*
import com.dwolla.aws.AccountId
import com.dwolla.aws.ArbitraryInstances.*
import com.dwolla.aws.ec2.model.Ec2InstanceId
import com.dwolla.RandomChunks
import fs2.{Chunk, Stream}
import org.scalacheck.*
import org.scalacheck.Arbitrary.arbitrary

import java.util.UUID
import scala.jdk.CollectionConverters.*

object ArbitraryInstances extends ArbitraryInstances

trait ArbitraryInstances {
  type ArbitraryCluster = List[Chunk[ClusterWithInstances]]
  type ClusterWithInstances = (Cluster, List[Chunk[ContainerInstance]])

  lazy val genRegion: Gen[Region] = Gen.oneOf(software.amazon.awssdk.regions.Region.regions().asScala).map(x => Region(x.id()))
  implicit lazy val arbRegion: Arbitrary[Region] = Arbitrary(genRegion)

  lazy val genContainerStatus: Gen[ContainerStatus] = Gen.oneOf(ContainerStatus.Active, ContainerStatus.Draining, ContainerStatus.Inactive)
  implicit lazy val arbContainerStatus: Arbitrary[ContainerStatus] = Arbitrary(genContainerStatus)

  lazy val genContainerInstance: Gen[ContainerInstance] =
    for {
      cId <- arbitrary[ContainerInstanceId]
      ec2Id <- arbitrary[Ec2InstanceId]
      taskCount <- arbitrary[TaskCount]
      status <- arbitrary[ContainerStatus]
    } yield ContainerInstance(cId, ec2Id, taskCount, status)
  implicit lazy val arbContainerInstance: Arbitrary[ContainerInstance] = Arbitrary(genContainerInstance)

  lazy val genClusterWithInstances: Gen[ClusterWithInstances] =
    for {
      seed <- arbitrary[Long]
      cluster <- arbitrary[Cluster]
      instances <- Gen.containerOf[List, ContainerInstance](arbContainerInstance.arbitrary).map(_.take(10)).map(Stream.emits(_).through(RandomChunks(100, seed)).chunks.toList)
    } yield cluster -> instances
  implicit lazy val arbClusterWithInstances: Arbitrary[ClusterWithInstances] = Arbitrary(genClusterWithInstances)

  lazy val genClustersWithInstances: Gen[ArbitraryCluster] = {
    Gen.long.flatMap { seed =>
      Gen.nonEmptyContainerOf[List, ClusterWithInstances](arbitrary[ClusterWithInstances])
        .suchThat { (f: List[(Cluster, List[Chunk[ContainerInstance]])]) =>
          lazy val containerInstanceCount = f.unorderedFoldMap(_._2.flatMap(_.toList).size)
          lazy val containerInstanceIdsAreUnique = f.flatMap(_._2.flatMap(_.toList).map(_.containerInstanceId)).toSet.size == containerInstanceCount
          lazy val ec2InstanceIdsAreUnique = f.flatMap(_._2.flatMap(_.toList).map(_.ec2InstanceId)).toSet.size == containerInstanceCount

          containerInstanceIdsAreUnique && ec2InstanceIdsAreUnique && containerInstanceCount > 0
        }
        .map(_.take(100))
        .map(Stream.emits(_).through(RandomChunks(100, seed)).chunks.toList)
    }
  }
  implicit lazy val arbClustersWithInstances: Arbitrary[ArbitraryCluster] = Arbitrary(genClustersWithInstances)
  implicit val shrinkArbitraryCluster: Shrink[ArbitraryCluster] = Shrink.shrinkAny

  lazy val genContainerInstanceId: Gen[ContainerInstanceId] =
    arbitrary[UUID].map(_.toString).map(ContainerInstanceId(_))
  implicit lazy val arbContainerInstanceId: Arbitrary[ContainerInstanceId] = Arbitrary(genContainerInstanceId)

  lazy val genCluster: Gen[Cluster] =
    for {
      region <- arbitrary[Region]
      accountId <- arbitrary[AccountId]
      clusterName <- arbitrary[ClusterName]
    } yield Cluster(region, accountId, clusterName)
  implicit lazy val arbCluster: Arbitrary[Cluster] = Arbitrary(genCluster)

  lazy val genClusterArn: Gen[ClusterArn] = genCluster.map(_.clusterArn)
  implicit lazy val arbClusterArn: Arbitrary[ClusterArn] = Arbitrary(genClusterArn)

  lazy val genClusterName: Gen[ClusterName] =
    Gen.oneOf("cluster1", "cluster2", "cluster3").map(ClusterName(_))
  implicit lazy val arbClusterName: Arbitrary[ClusterName] = Arbitrary(genClusterName)

  lazy val genTaskCount: Gen[TaskCount] =
    Gen.chooseNum(0, 20).map(TaskCount(_))
  implicit lazy val arbTaskCount: Arbitrary[TaskCount] = Arbitrary(genTaskCount)
}
