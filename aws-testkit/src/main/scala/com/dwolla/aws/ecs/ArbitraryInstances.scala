package com.dwolla.aws.ecs

import cats.syntax.all.*
import com.dwolla.aws.AccountId
import com.dwolla.aws.ArbitraryInstances.*
import com.dwolla.aws.ec2.Ec2InstanceId
import com.dwolla.RandomChunks
import fs2.{Chunk, Stream}
import monix.newtypes.NewtypeWrapped
import org.scalacheck.*
import org.scalacheck.Arbitrary.arbitrary

import java.util.UUID
import scala.jdk.CollectionConverters.*

object ArbitraryInstances extends ArbitraryInstances

trait ArbitraryInstances {
  type ArbitraryCluster = ArbitraryCluster.Type
  object ArbitraryCluster extends NewtypeWrapped[List[Chunk[ClusterWithInstances]]]

  type ClusterWithInstances = ClusterWithInstances.Type
  object ClusterWithInstances extends NewtypeWrapped[(Cluster, List[Chunk[ContainerInstance]])] {
    extension (cwi: ClusterWithInstances) {
      def cluster: Cluster = cwi.value._1
      def pages: List[Chunk[ContainerInstance]] = cwi.value._2
    }
  }

  lazy val genRegion: Gen[Region] = Gen.oneOf(software.amazon.awssdk.regions.Region.regions().asScala).map(x => Region(x.id()))
  implicit lazy val arbRegion: Arbitrary[Region] = Arbitrary(genRegion)

  lazy val genContainerInstanceStatus: Gen[ContainerInstanceStatus] = Gen.oneOf(ContainerInstanceStatus.Active, ContainerInstanceStatus.Draining, ContainerInstanceStatus.Inactive)
  implicit lazy val arbContainerInstanceStatus: Arbitrary[ContainerInstanceStatus] = Arbitrary(genContainerInstanceStatus)

  lazy val genContainerInstance: Gen[ContainerInstance] =
    for {
      cId <- arbitrary[ContainerInstanceId]
      ec2Id <- arbitrary[Ec2InstanceId]
      taskCount <- arbitrary[TaskCount]
      status <- arbitrary[ContainerInstanceStatus]
    } yield ContainerInstance(cId, ec2Id, taskCount, status)
  implicit lazy val arbContainerInstance: Arbitrary[ContainerInstance] = Arbitrary(genContainerInstance)

  lazy val genClusterWithInstances: Gen[ClusterWithInstances] =
    for {
      seed <- arbitrary[Long]
      cluster <- arbitrary[Cluster]
      instances <- Gen.containerOf[List, ContainerInstance](arbContainerInstance.arbitrary).map(_.take(10)).map(Stream.emits(_).through(RandomChunks(100, seed)).chunks.toList)
    } yield ClusterWithInstances(cluster -> instances)
  implicit lazy val arbClusterWithInstances: Arbitrary[ClusterWithInstances] = Arbitrary(genClusterWithInstances)

  lazy val genClustersWithInstances: Gen[ArbitraryCluster] =
    Gen.long.flatMap { seed =>
      Gen.nonEmptyContainerOf[List, ClusterWithInstances](arbitrary[ClusterWithInstances])
        .suchThat { (f: List[ClusterWithInstances]) =>
          lazy val containerInstanceCount = f.unorderedFoldMap(_.value._2.flatMap(_.toList).size)
          lazy val containerInstanceIdsAreUnique = f.flatMap(_.value._2.flatMap(_.toList).map(_.containerInstanceId)).toSet.size == containerInstanceCount
          lazy val ec2InstanceIdsAreUnique = f.flatMap(_.value._2.flatMap(_.toList).map(_.ec2InstanceId)).toSet.size == containerInstanceCount

          containerInstanceIdsAreUnique && ec2InstanceIdsAreUnique && containerInstanceCount > 0
        }
        .map(_.take(100))
        .map(Stream.emits(_).through(RandomChunks(100, seed)).chunks.toList)
        .map(ArbitraryCluster(_))
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

  val genTaskStatus: Gen[TaskStatus] =
    Gen.frequency(1 -> TaskStatus.Pending, 10 -> TaskStatus.Running, 1 -> TaskStatus.Stopped)
  implicit val arbTaskStatus: Arbitrary[TaskStatus] = Arbitrary(genTaskStatus)

  val genTaskArn: Gen[TaskArn] =
    for {
      region <- genRegion
      accountId <- arbitrary[AccountId]
      taskId <- Gen.identifier
    } yield TaskArn(s"arn:aws:ecs:$region:$accountId:task/$taskId")
  implicit val arbTaskArn: Arbitrary[TaskArn] = Arbitrary(genTaskArn)

  val genTask: Gen[(TaskArn, TaskStatus, TaskDefinitionArn)] =
    for {
      arn <- genTaskArn
      status <- arbitrary[TaskStatus]
      defnArn <- arbitrary[TaskDefinitionArn]
    } yield (arn, status, defnArn)
  val genListTaskPages: Gen[ListTaskPages] =
    Gen.nonEmptyListOf(Gen.nonEmptyListOf(genTask)).map(ListTaskPages(_))
  implicit val arbListTaskPages: Arbitrary[ListTaskPages] = Arbitrary(genListTaskPages)

  val genTaskDefinitionArn: Gen[TaskDefinitionArn] =
    for {
      region <- genRegion
      accountId <- arbitrary[AccountId]
      name <- Gen.identifier
      revision <- Gen.posNum[Int]
    } yield TaskDefinitionArn(s"arn:aws:ecs:$region:$accountId:task-definition/$name:$revision")
  implicit val arbTaskDefinitionArn: Arbitrary[TaskDefinitionArn] = Arbitrary(genTaskDefinitionArn)
}

type ListTaskPages = ListTaskPages.Type
object ListTaskPages extends NewtypeWrapped[List[List[(TaskArn, TaskStatus, TaskDefinitionArn)]]]
