package com.dwolla.aws.ecs

import cats.*
import cats.data.OptionT
import cats.effect.*
import cats.implicits.*
import com.amazonaws.ecs.ContainerInstanceStatus.DRAINING
import com.amazonaws.ecs.{BoxedInteger, ContainerInstanceField, DescribeContainerInstancesResponse, ECS, ListClustersResponse, ListContainerInstancesResponse, UpdateContainerInstancesStateResponse, ContainerInstance as AwsContainerInstance}
import com.dwolla.aws.ArbitraryInstances
import com.dwolla.NextPageTokens.tokenForIdx
import com.dwolla.aws.ecs.*
import fs2.{Chunk, Stream}
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.effect.PropF.forAllF
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.noop.NoOpFactory

class EcsAlgImplSpec 
  extends CatsEffectSuite
    with ScalaCheckEffectSuite
    with ArbitraryInstances {

  private implicit def loggerFactory[F[_] : Applicative]: LoggerFactory[F] = NoOpFactory[F]

  def fakeECS[F[_] : ApplicativeThrow](arbCluster: ArbitraryCluster): ECS[F] = new FakeECS[F] {
    private val listClustersResponses: Map[Option[String], ListClustersResponse] = {
      val calculated = arbCluster.zipWithIndex.map {
        case (chunk, idx) =>
          val result = ListClustersResponse(
            chunk.map { case (c, _) => c.clusterArn.value }.toList.some,
            tokenForIdx(idx + 1, arbCluster.length)
          )

          tokenForIdx(idx, arbCluster.length) -> result
      }

      Map(calculated *).withDefaultValue(ListClustersResponse())
    }

    private val listContainerInstancesResponses: Map[(Option[ClusterArn], Option[String]), ListContainerInstancesResponse] =
      Map.from {
        arbCluster.flatMap(_.toList).flatMap {
          case (c, ciChunks) =>
            ciChunks
              .zipWithIndex
              .map {
                case (chunk, idx) =>
                  val request = c.clusterArn.some -> tokenForIdx(idx, ciChunks.length)
                  val result = ListContainerInstancesResponse(
                    containerInstanceArns = chunk.map(_.containerInstanceId.value).toList.some,
                    nextToken = tokenForIdx(idx + 1, ciChunks.length),
                  )

                  request -> result
              }
        }
      }.withDefaultValue(ListContainerInstancesResponse())

    private def ciToCi(ci: ContainerInstance): AwsContainerInstance =
      AwsContainerInstance(
        containerInstanceArn = ci.containerInstanceId.value.some,
        ec2InstanceId = ci.ec2InstanceId.value.some,
        runningTasksCount = ci.runningTaskCount.value,
        status = ci.status.toString.some,
      )

    private val clusterMap: Map[ClusterArn, List[ContainerInstance]] = Foldable[List].fold {
      arbCluster
        .flatMap(_.toList)
        .flatMap {
          case (c: Cluster, cis: List[Chunk[ContainerInstance]]) =>
            cis
              .flatMap(_.toList)
              .map(ci => Map(c.clusterArn -> List(ci)))
        }
    }

    override def listClusters(nextToken: Option[String], maxResults: Option[BoxedInteger]): F[ListClustersResponse] =
      listClustersResponses(nextToken).pure[F]

    override def listContainerInstances(cluster: Option[String],
                                        filter: Option[String],
                                        nextToken: Option[String],
                                        maxResults: Option[BoxedInteger],
                                        status: Option[com.amazonaws.ecs.ContainerInstanceStatus]): F[ListContainerInstancesResponse] = {
      listContainerInstancesResponses(cluster.map(ClusterArn(_)) -> nextToken).pure[F]
    }

    override def describeContainerInstances(containerInstances: List[String],
                                            cluster: Option[String],
                                            include: Option[List[ContainerInstanceField]]): F[DescribeContainerInstancesResponse] = {
      val requestedCluster = ClusterArn(cluster.getOrElse("default"))
      val allInstances = clusterMap.getOrElse(requestedCluster, List.empty)
      val cis: List[ContainerInstance] = allInstances.filter(i => containerInstances.contains(i.containerInstanceId.value))

      DescribeContainerInstancesResponse(containerInstances = cis.map(ciToCi).some).pure[F]
    }
  }

  test("EcsAlg should return all the cluster ARNs") {
    forAllF { (arbCluster: ArbitraryCluster) =>
      EcsAlg[IO](fakeECS(arbCluster))
        .listClusterArns
        .compile
        .toList
        .map { obtained =>
          val expected = arbCluster.flatMap(_.toList).map(tuple => tuple._1.clusterArn)
          assertEquals(obtained, expected)
        }
    }
  }

  test("EcsAlg should return all the container instances for the given cluster") {
    forAllF { (arbCluster: ArbitraryCluster) =>
      val testClass = EcsAlg[IO](fakeECS(arbCluster))

      Stream.emits(arbCluster)
        .unchunks
        .covary[IO]
        .evalMap { case (expectedCluster, expectedContainerInstanceChunks) =>
          testClass
            .listContainerInstances(expectedCluster.clusterArn)
            .compile
            .toList
            .tupleRight(expectedContainerInstanceChunks.flatMap(_.toList))
        }
        .map { case (obtained, expected) =>
          assertEquals(obtained, expected)
        }
        .compile
        .foldMonoid
    }
  }

  test("EcsAlg should return the matching container instance when asked to search by EC2 Instance ID") {
    forAllF { (arbCluster: ArbitraryCluster) =>
      val testClass = EcsAlg[IO](fakeECS(arbCluster))
      val (expectedCluster, expectedCI) = arbCluster.flatMap(_.toList).find(_._2.flatMap(_.toList).nonEmpty).flatMap {
        case (c, cis) => cis.flatMap(_.toList).lastOption.map(c -> _)
      }.getOrElse(throw new RuntimeException("test setup exception; no container instances were found in the arbitrary cluster"))

      OptionT(testClass.findEc2Instance(expectedCI.ec2InstanceId))
        .fold(fail(s"findEc2Instance(${expectedCI.ec2InstanceId}) returned None")) { case (actualCluster, actualCI) =>
          assertEquals(actualCluster, expectedCluster.clusterArn)
          assertEquals(actualCI, expectedCI)
        }
    }
  }

  test("EcsAlg should update the given instance's status to Draining if it's not already") {
    forAllF { (cluster: ClusterArn, ci: ContainerInstance) =>
      val activeContainerInstance = ci.copy(status = ContainerInstanceStatus.Active)

      for {
        deferredContainerInstances <- Deferred[IO, List[ContainerInstanceId]]
        deferredStatus <- Deferred[IO, com.amazonaws.ecs.ContainerInstanceStatus]
        deferredCluster <- Deferred[IO, Option[ClusterArn]]
        fakeEcsClient: ECS[IO] = new FakeECS[IO] {
              override def updateContainerInstancesState(containerInstances: List[String],
                                                         status: com.amazonaws.ecs.ContainerInstanceStatus,
                                                         cluster: Option[String]): IO[UpdateContainerInstancesStateResponse] =
                deferredContainerInstances.complete(containerInstances.map(ContainerInstanceId(_))) >>
                  deferredStatus.complete(status) >>
                  deferredCluster.complete(cluster.map(ClusterArn(_)))
                    .as(UpdateContainerInstancesStateResponse())
            }
        _ <- EcsAlg[IO](fakeEcsClient).drainInstance(cluster, activeContainerInstance)
        actualContainerInstances <- deferredContainerInstances.get
        actualStatus <- deferredStatus.get
        actualCluster <- deferredCluster.get
      } yield {
        assertEquals(actualCluster, cluster.some)
        assert(actualContainerInstances.contains(activeContainerInstance.containerInstanceId))
        assertEquals(actualStatus, DRAINING)
      }
    }
  }

  test("EcsAlg should ignore requests to change the status of instances that are already draining") {
    forAllF { (cluster: ClusterArn, ci: ContainerInstance) =>
      val activeContainerInstance = ci.copy(status = ContainerInstanceStatus.Draining)

      EcsAlg[IO](new FakeECS[IO] {}).drainInstance(cluster, activeContainerInstance)
    }
  }
}
