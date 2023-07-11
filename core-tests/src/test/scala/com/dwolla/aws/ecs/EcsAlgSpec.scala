package com.dwolla.aws.ecs

import cats.*
import cats.data.*
import cats.effect.*
import cats.syntax.all.*
import com.amazonaws.ecs.ContainerInstanceStatus.DRAINING
import com.amazonaws.ecs.{BoxedInteger, ContainerInstanceField, DescribeContainerInstancesResponse, DescribeTasksResponse, DesiredStatus, ECS, LaunchType, ListClustersResponse, ListContainerInstancesResponse, ListTasksResponse, Task, TaskField, UpdateContainerInstancesStateResponse, ContainerInstance as AwsContainerInstance}
import com.dwolla.aws.ArbitraryInstances
import com.dwolla.aws.ecs.*
import fs2.{Chunk, Stream}
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.effect.PropF.forAllF
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.noop.NoOpFactory
import com.dwolla.*
import com.dwolla.aws.ArbitraryPagination

import scala.annotation.nowarn

@nowarn("""msg=pattern selector should be an instance of Matchable[,]+\s+but it has unmatchable type EcsAlgSpec\.this\.ClusterWithInstances(?:\.Type)? instead""")
class EcsAlgSpec
  extends CatsEffectSuite
    with ScalaCheckEffectSuite
    with ArbitraryInstances {

  private implicit def loggerFactory[F[_] : Applicative]: LoggerFactory[F] = NoOpFactory[F]

  def fakeECS[F[_] : ApplicativeThrow](arbCluster: ArbitraryCluster): ECS[F] = new FakeECS[F] {
    private val listClustersResponses: Map[NextPageToken, ListClustersResponse] =
      ArbitraryPagination.paginateWith[Chunk, ArbitraryCluster, ClusterWithInstances, ClusterArn](arbCluster) {
        case ClusterWithInstances((c, _)) => c.clusterArn
      }
        .view
        .mapValues {
          case (c, n) =>
            ListClustersResponse(c.toList.map(_.value).some, n.value)
        }
        .toMap

    private val listContainerInstancesResponses: Map[Option[ClusterArn], Map[NextPageToken, ListContainerInstancesResponse]] =
      arbCluster
        .value
        .flatMap(_.toList)
        .map { cwi =>
          val clusterArn: Option[ClusterArn] = cwi.value._1.clusterArn.some
          val pages = ArbitraryPagination.paginate(cwi.value._2).view.mapValues {
            case (c, n) =>
              ListContainerInstancesResponse(c.map(_.containerInstanceId.value).toList.some, n.value)
          }
            .toMap

          clusterArn -> pages
        }
        .toMap

    private def ciToCi(ci: ContainerInstance): AwsContainerInstance =
      AwsContainerInstance(
        containerInstanceArn = ci.containerInstanceId.value.some,
        ec2InstanceId = ci.ec2InstanceId.value.some,
        runningTasksCount = ci.runningTaskCount.value,
        status = ci.status.toString.some,
      )

    private val clusterMap: Map[ClusterArn, List[ContainerInstance]] = Foldable[List].fold {
      arbCluster
        .value
        .flatMap(_.toList)
        .flatMap {
          case ClusterWithInstances((c, cis)) =>
            cis
              .flatMap(_.toList)
              .map(ci => Map(c.clusterArn -> List(ci)))
        }
    }

    override def listClusters(nextToken: Option[String], maxResults: Option[BoxedInteger]): F[ListClustersResponse] =
      listClustersResponses(NextPageToken(nextToken)).pure[F]

    override def listContainerInstances(cluster: Option[String],
                                        filter: Option[String],
                                        nextToken: Option[String],
                                        maxResults: Option[BoxedInteger],
                                        status: Option[com.amazonaws.ecs.ContainerInstanceStatus]): F[ListContainerInstancesResponse] =
      listContainerInstancesResponses
        .get(cluster.map(ClusterArn(_)))
        .flatMap(_.get(NextPageToken(nextToken)))
        .getOrElse(ListContainerInstancesResponse())
        .pure[F]

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
          val expected = arbCluster.value.flatMap(_.toList).map(_.cluster.clusterArn)
          assertEquals(obtained, expected)
        }
    }
  }

  test("EcsAlg should return all the container instances for the given cluster") {
    forAllF { (arbCluster: ArbitraryCluster) =>
      val testClass = EcsAlg[IO](fakeECS(arbCluster))

      Stream.emits(arbCluster.value)
        .unchunks
        .covary[IO]
        .evalMap { case ClusterWithInstances((expectedCluster, expectedContainerInstanceChunks)) =>
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
      val (expectedCluster, expectedCI) = arbCluster.value.flatMap(_.toList).find(_.pages.flatMap(_.toList).nonEmpty).flatMap {
        case ClusterWithInstances((c, cis)) => cis.flatMap(_.toList).lastOption.map(c -> _)
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

  test("EcsAlg should be able to determine if an ECS Task is running on a given Container Instance") {
    forAllF { (arbCluster: ClusterArn,
               arbCi: ContainerInstance,
               arbTaskDefinition: TaskDefinitionArn,
               arbTaskStatus: Option[(TaskArn, TaskStatus)],
               arbOtherTasks: ListTaskPages,
              ) =>
      val allTaskPages = arbTaskStatus.foldl(arbOtherTasks.value) { case (otherTasks, (taskArn, status)) =>
        otherTasks appended List((taskArn, status, arbTaskDefinition))
      }

      val taskPages: Map[NextPageToken, (List[TaskArn], NextPageToken)] =
        ArbitraryPagination.paginateWith(allTaskPages) {
          case (arn, _, _) => arn
        }

      val allTasks: Map[TaskArn, (TaskStatus, TaskDefinitionArn)] =
        allTaskPages.flatten.map { case (a, s, tda) => a -> (s, tda) }.toMap

      val alg = EcsAlg(new FakeECS[IO] {
        override def listTasks(cluster: Option[String],
                               containerInstance: Option[String],
                               family: Option[String],
                               nextToken: Option[String],
                               maxResults: Option[BoxedInteger],
                               startedBy: Option[String],
                               serviceName: Option[String],
                               desiredStatus: Option[DesiredStatus],
                               launchType: Option[LaunchType]): IO[ListTasksResponse] = IO.pure {
          if (cluster.contains(arbCluster.value) && containerInstance.contains(arbCi.containerInstanceId.value) && family.isEmpty) {
            ListTasksResponse.apply.tupled {
              taskPages.get(NextPageToken(nextToken))
                .separate
                .map(_.flatMap(_.value))
                .leftMap(_.map(_.map(_.value)))
            }
          } else {
            ListTasksResponse(None, None)
          }
        }

        override def describeTasks(tasks: List[String],
                                   cluster: Option[String],
                                   include: Option[List[TaskField]]): IO[DescribeTasksResponse] =
          DescribeTasksResponse(tasks = Option.when(cluster.contains(arbCluster.value)) {
            tasks
              .map(TaskArn(_))
              .mproduct(allTasks.get(_).toList)
              .map { case (taskArn, (status, taskDefinitionArn)) =>
                Task(
                  clusterArn = cluster,
                  lastStatus = status.toString.some,
                  taskDefinitionArn = taskDefinitionArn.value.some,
                  taskArn = taskArn.value.some,
                )
              }
          }).pure[IO]
      })

      for {
        output <- alg.isTaskDefinitionRunningOnInstance(arbCluster, arbCi, arbTaskDefinition)
      } yield {
        assertEquals(output, arbTaskStatus.map(_._2).contains(TaskStatus.Running)) // TODO figure out why this compiled without the .map(_._2) part before
      }
    }
  }
}
