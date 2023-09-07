package com.dwolla.aws.ecs

import cats.syntax.all.*
import com.dwolla.RandomChunks
import com.dwolla.aws.ec2.{Ec2InstanceId, given}
import com.dwolla.aws.{AccountId, given}
import fs2.{Chunk, Stream}
import monix.newtypes.NewtypeWrapped
import org.scalacheck.*
import org.scalacheck.Arbitrary.arbitrary

import java.util.UUID
import scala.jdk.CollectionConverters.*

type ArbitraryCluster = ArbitraryCluster.Type
object ArbitraryCluster extends NewtypeWrapped[List[Chunk[ClusterWithInstances]]]

type ClusterWithInstances = ClusterWithInstances.Type
object ClusterWithInstances extends NewtypeWrapped[(Cluster, List[Chunk[ContainerInstance]])] {
  extension (cwi: ClusterWithInstances) {
    def cluster: Cluster = cwi.value._1
    def pages: List[Chunk[ContainerInstance]] = cwi.value._2
  }
}

def genRegion: Gen[Region] = Gen.oneOf(software.amazon.awssdk.regions.Region.regions().asScala).map(x => Region(x.id()))
given Arbitrary[Region] = Arbitrary(genRegion)

def genContainerInstanceStatus: Gen[ContainerInstanceStatus] = Gen.oneOf(ContainerInstanceStatus.Active, ContainerInstanceStatus.Draining, ContainerInstanceStatus.Inactive)
given Arbitrary[ContainerInstanceStatus] = Arbitrary(genContainerInstanceStatus)

def genContainerInstance: Gen[ContainerInstance] =
  for {
    cId <- arbitrary[ContainerInstanceId]
    ec2Id <- arbitrary[Ec2InstanceId]
    taskCount <- arbitrary[TaskCount]
    status <- arbitrary[ContainerInstanceStatus]
  } yield ContainerInstance(cId, ec2Id, taskCount, status)
given Arbitrary[ContainerInstance] = Arbitrary(genContainerInstance)

def genClusterWithInstances: Gen[ClusterWithInstances] =
  for {
    seed <- arbitrary[Long]
    cluster <- arbitrary[Cluster]
    instances <- Gen.containerOf[List, ContainerInstance](genContainerInstance).map(_.take(10)).map(Stream.emits(_).through(RandomChunks(100, seed)).chunks.toList)
  } yield ClusterWithInstances(cluster -> instances)
given Arbitrary[ClusterWithInstances] = Arbitrary(genClusterWithInstances)

def genClustersWithInstances: Gen[ArbitraryCluster] =
  Gen.long.flatMap { seed =>
    Gen.nonEmptyContainerOf[List, ClusterWithInstances](arbitrary[ClusterWithInstances])
      .suchThat { (f: List[ClusterWithInstances]) =>
        def containerInstanceCount = f.unorderedFoldMap(_.value._2.flatMap(_.toList).size)
        def containerInstanceIdsAreUnique = f.flatMap(_.value._2.flatMap(_.toList).map(_.containerInstanceId)).toSet.size == containerInstanceCount
        def ec2InstanceIdsAreUnique = f.flatMap(_.value._2.flatMap(_.toList).map(_.ec2InstanceId)).toSet.size == containerInstanceCount

        containerInstanceIdsAreUnique && ec2InstanceIdsAreUnique && containerInstanceCount > 0
      }
      .map(_.take(100))
      .map(Stream.emits(_).through(RandomChunks(100, seed)).chunks.toList)
      .map(ArbitraryCluster(_))
  }
given Arbitrary[ArbitraryCluster] = Arbitrary(genClustersWithInstances)
given Shrink[ArbitraryCluster] = Shrink.shrinkAny

def genContainerInstanceId: Gen[ContainerInstanceId] =
  arbitrary[UUID].map(_.toString).map(ContainerInstanceId(_))
given Arbitrary[ContainerInstanceId] = Arbitrary(genContainerInstanceId)

def genCluster: Gen[Cluster] =
  for {
    region <- arbitrary[Region]
    accountId <- arbitrary[AccountId]
    clusterName <- arbitrary[ClusterName]
  } yield Cluster(region, accountId, clusterName)
given Arbitrary[Cluster] = Arbitrary(genCluster)

def genClusterArn: Gen[ClusterArn] = genCluster.map(_.clusterArn)
given Arbitrary[ClusterArn] = Arbitrary(genClusterArn)

def genClusterName: Gen[ClusterName] =
  Gen.oneOf("cluster1", "cluster2", "cluster3").map(ClusterName(_))
given Arbitrary[ClusterName] = Arbitrary(genClusterName)

def genTaskCount: Gen[TaskCount] =
  Gen.chooseNum(0, 20).map(TaskCount(_))
given Arbitrary[TaskCount] = Arbitrary(genTaskCount)

val genTaskStatus: Gen[TaskStatus] =
  Gen.frequency(1 -> TaskStatus.Pending, 10 -> TaskStatus.Running, 1 -> TaskStatus.Stopped)
given Arbitrary[TaskStatus] = Arbitrary(genTaskStatus)

val genTaskArn: Gen[TaskArn] =
  for {
    region <- genRegion
    accountId <- arbitrary[AccountId]
    taskId <- Gen.identifier
  } yield TaskArn(s"arn:aws:ecs:$region:$accountId:task/$taskId")
given Arbitrary[TaskArn] = Arbitrary(genTaskArn)

val genTask: Gen[(TaskArn, TaskStatus, TaskDefinitionArn)] =
  for {
    arn <- genTaskArn
    status <- arbitrary[TaskStatus]
    defnArn <- arbitrary[TaskDefinitionArn]
  } yield (arn, status, defnArn)
val genListTaskPages: Gen[ListTaskPages] =
  Gen.nonEmptyListOf(Gen.nonEmptyListOf(genTask)).map(ListTaskPages(_))
given Arbitrary[ListTaskPages] = Arbitrary(genListTaskPages)

val genTaskDefinitionArn: Gen[TaskDefinitionArn] =
  for {
    region <- genRegion
    accountId <- arbitrary[AccountId]
    name <- Gen.identifier
    revision <- Gen.posNum[Int]
  } yield TaskDefinitionArn(s"arn:aws:ecs:$region:$accountId:task-definition/$name:$revision")
given Arbitrary[TaskDefinitionArn] = Arbitrary(genTaskDefinitionArn)

type ListTaskPages = ListTaskPages.Type
object ListTaskPages extends NewtypeWrapped[List[List[(TaskArn, TaskStatus, TaskDefinitionArn)]]]
