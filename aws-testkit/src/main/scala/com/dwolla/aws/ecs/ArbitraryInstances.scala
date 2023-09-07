package com.dwolla.aws.ecs

import cats.syntax.all.*
import com.dwolla.aws.{AccountId, given}
import com.dwolla.aws.ec2.{Ec2InstanceId, given}
import com.dwolla.RandomChunks
import com.dwolla.aws.ecs.TaskStatus.stoppedTaskStatuses
import fs2.{Chunk, Pure, Stream}
import monix.newtypes.NewtypeWrapped
import org.scalacheck.*
import org.scalacheck.Arbitrary.arbitrary

import java.util.UUID
import scala.jdk.CollectionConverters.*

case class TaskArnAndStatus(arn: TaskArn, status: TaskStatus)
case class ContainerInstanceWithTaskPages(containerInstanceId: ContainerInstanceId,
                                          ec2InstanceId: Ec2InstanceId,
                                          tasks: List[Chunk[TaskArnAndStatus]],
                                          status: ContainerInstanceStatus,
                                         ) {
  def toContainerInstance: ContainerInstance =
    ContainerInstance(
      containerInstanceId,
      ec2InstanceId,
      TaskCount(tasks.foldMap(_.filterNot {
        case TaskArnAndStatus(_, status) => stoppedTaskStatuses.contains(status)
      }.size).toLong),
      status)
}

type ArbitraryCluster = ArbitraryCluster.Type
object ArbitraryCluster extends NewtypeWrapped[List[Chunk[ClusterWithInstances]]]

type ClusterWithInstances = ClusterWithInstances.Type
object ClusterWithInstances extends NewtypeWrapped[(Cluster, List[Chunk[ContainerInstanceWithTaskPages]])] {
  extension (cwi: ClusterWithInstances) {
    def cluster: Cluster = cwi.value._1
    def pages: List[Chunk[ContainerInstanceWithTaskPages]] = cwi.value._2
  }
}

type TasksForContainerInstance = TasksForContainerInstance.Type
object TasksForContainerInstance extends NewtypeWrapped[List[Chunk[TaskArnAndStatus]]]

def genRegion: Gen[Region] = Gen.oneOf(software.amazon.awssdk.regions.Region.regions().asScala).map(x => Region(x.id()))
given Arbitrary[Region] = Arbitrary(genRegion)

def genContainerInstanceStatus: Gen[ContainerInstanceStatus] = Gen.oneOf(ContainerInstanceStatus.Active, ContainerInstanceStatus.Draining, ContainerInstanceStatus.Inactive)
given Arbitrary[ContainerInstanceStatus] = Arbitrary(genContainerInstanceStatus)

def genContainerInstanceWithTaskPages: Gen[ContainerInstanceWithTaskPages] =
  for {
    cId <- arbitrary[ContainerInstanceId]
    ec2Id <- arbitrary[Ec2InstanceId]
    taskCount <- arbitrary[TasksForContainerInstance]
    status <- arbitrary[ContainerInstanceStatus]
  } yield ContainerInstanceWithTaskPages(cId, ec2Id, taskCount.value, status)
given Arbitrary[ContainerInstanceWithTaskPages] = Arbitrary(genContainerInstanceWithTaskPages)

def genContainerInstance: Gen[ContainerInstance] =
  arbitrary[ContainerInstanceWithTaskPages].map(_.toContainerInstance)
given Arbitrary[ContainerInstance] = Arbitrary(genContainerInstance)

def genTaskArnAndStatus: Gen[TaskArnAndStatus] =
  for {
    arn <- arbitrary[TaskArn]
    status <- arbitrary[TaskStatus]
  } yield TaskArnAndStatus(arn, status)
given Arbitrary[TaskArnAndStatus] = Arbitrary(genTaskArnAndStatus)

private def chunkList[A](seed: Long)
                        (list: List[A]): List[Chunk[A]] =
  Stream.emits(list)
    .through(RandomChunks[Pure, A](100, seed))
    .chunks
    .toList

def genTasksForContainerInstance: Gen[TasksForContainerInstance] =
  for {
    seed <- arbitrary[Long]
    totalTasks <- Gen.chooseNum(0, 40)
    tasks <- Gen.containerOfN[List, TaskArnAndStatus](totalTasks, arbitrary[TaskArnAndStatus]).map(chunkList(seed))
  } yield TasksForContainerInstance(tasks)
given Arbitrary[TasksForContainerInstance] = Arbitrary(genTasksForContainerInstance)

def genClusterWithInstances(containerBuilder: [T] => Gen[T] => Gen[List[T]]): Gen[ClusterWithInstances] =
  for {
    seed <- arbitrary[Long]
    cluster <- arbitrary[Cluster]
    instances <- containerBuilder(genContainerInstanceWithTaskPages).map(_.take(10)).map(Stream.emits(_).through(RandomChunks(100, seed)).chunks.toList)
  } yield ClusterWithInstances(cluster -> instances)
given Arbitrary[ClusterWithInstances] = Arbitrary(genClusterWithInstances([X] => Gen.containerOf[List, X](_: Gen[X])))

def genClustersWithInstances: Gen[ArbitraryCluster] =
  Gen.long.flatMap { seed =>
    Gen.nonEmptyContainerOf[List, ClusterWithInstances](genClusterWithInstances([X] => Gen.nonEmptyContainerOf[List, X](_: Gen[X])))
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
given Arbitrary[ArbitraryCluster] = Arbitrary(genClustersWithInstances)
implicit val shrinkArbitraryCluster: Shrink[ArbitraryCluster] = Shrink.shrinkAny

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
  Gen.frequency(
    1 -> TaskStatus.Provisioning,
      1 -> TaskStatus.Pending,
      1 -> TaskStatus.Activating,
      10 -> TaskStatus.Running,
      1 -> TaskStatus.Deactivating,
      1 -> TaskStatus.Stopping,
      1 -> TaskStatus.Deprovisioning,
      5 -> TaskStatus.Stopped,
      5 -> TaskStatus.Deleted,
  )
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
