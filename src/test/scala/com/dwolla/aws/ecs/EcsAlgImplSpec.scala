package com.dwolla.aws.ecs

import java.util.concurrent.Future

import cats._
import cats.effect._
import cats.effect.concurrent.Deferred
import cats.implicits._
import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.services.ecs.AmazonECSAsync
import com.amazonaws.services.ecs.model.{ContainerInstance => AwsContainerInstance, _}
import com.dwolla.RandomChunks
import com.dwolla.aws.ArbitraryInstances._
import com.dwolla.aws.NextPageTokens._
import com.dwolla.aws.ecs.model.ContainerStatus.Draining
import com.dwolla.aws.ecs.model.{ClusterArn, ContainerInstance, ContainerStatus, tagClusterArn}
import fs2.Stream
import org.specs2.ScalaCheck
import org.specs2.matcher.{IOImplicits, IOMatchers}
import org.specs2.mutable.Specification

import scala.collection.JavaConverters._

class EcsAlgImplSpec extends Specification with ScalaCheck with IOMatchers with IOImplicits {

  def fakeECSAsync(arbCluster: ArbitraryCluster): AmazonECSAsync = new FakeECSAsync {
    private def listClustersResponses(arbCluster: ArbitraryCluster): Map[Option[String], ListClustersResult] = {
      val clusterChunks = Stream.emits(arbCluster).through(RandomChunks(100)).chunks.toList
      val calculated = clusterChunks.zipWithIndex.map {
        case (chunk, idx) =>
          val result = new ListClustersResult().withClusterArns(chunk.map { case (c, _) => c.clusterArn }.toList: _*).withNextToken(idx + 1, clusterChunks.length)

          tokenForIdx(idx, clusterChunks.length) -> result
      }

      Map(calculated: _*).withDefaultValue(new ListClustersResult())
    }

    private def listContainerInstancesResponses(arbCluster: ArbitraryCluster): Map[(ClusterArn, Option[String]), ListContainerInstancesResult] =
      Map(arbCluster.flatMap {
        case (c, cis) =>
          val ciChunks = Stream.emits(cis)
            .through(RandomChunks(100))
            .chunks
            .toList
          ciChunks
            .zipWithIndex
            .map {
              case (chunk, idx) =>
                val request = c.clusterArn -> tokenForIdx(idx, ciChunks.length)
                val result = new ListContainerInstancesResult().withContainerInstanceArns(chunk.map(_.containerInstanceId).toList: _*).withNextToken(idx + 1, ciChunks.length)

                request -> result
            }
      }: _*).withDefaultValue(new ListContainerInstancesResult())

    private def ciToCi(ci: ContainerInstance): AwsContainerInstance =
      new AwsContainerInstance()
        .withContainerInstanceArn(ci.containerInstanceId)
        .withEc2InstanceId(ci.ec2InstanceId)
        .withRunningTasksCount(ci.runningTaskCount)
        .withStatus(ci.status.toString)

    private val cachedListClustersResponses = listClustersResponses(arbCluster)
    private val cachedListContainerInstancesResponses = listContainerInstancesResponses(arbCluster)
    private val clusterMap: Map[ClusterArn, List[ContainerInstance]] = Foldable[List].fold(arbCluster.flatMap {
      case (c, cis) =>
        cis.map(ci => Map(c.clusterArn -> List(ci)))
    })

    override def listClustersAsync(req: ListClustersRequest,
                                   asyncHandler: AsyncHandler[ListClustersRequest, ListClustersResult]): Future[ListClustersResult] = {
      asyncHandler.onSuccess(req, cachedListClustersResponses(Option(req.getNextToken)))
      null
    }

    override def listContainerInstancesAsync(req: ListContainerInstancesRequest,
                                             asyncHandler: AsyncHandler[ListContainerInstancesRequest, ListContainerInstancesResult]): Future[ListContainerInstancesResult] = {
      asyncHandler.onSuccess(req, cachedListContainerInstancesResponses(tagClusterArn(req.getCluster) -> Option(req.getNextToken)))
      null
    }

    override def describeContainerInstancesAsync(req: DescribeContainerInstancesRequest,
                                                 asyncHandler: AsyncHandler[DescribeContainerInstancesRequest, DescribeContainerInstancesResult]): Future[DescribeContainerInstancesResult] = {
      val requestedCluster = Option(req.getCluster).getOrElse("default").asInstanceOf[ClusterArn]
      val allInstances = clusterMap.getOrElse(requestedCluster, List.empty)
      val cis: List[ContainerInstance] = allInstances.filter(i => req.getContainerInstances.contains(i.containerInstanceId))
      val result = new DescribeContainerInstancesResult().withContainerInstances(cis.map(ciToCi): _*)

      asyncHandler.onSuccess(req, result)
      null
    }
  }

  "EcsAlg" should {
    "return all the cluster ARNs" >> prop { arbCluster: ArbitraryCluster =>
      val testClass = EcsAlg[IO](fakeECSAsync(arbCluster))

      for {
        arns <- testClass.listClusterArns.compile.toList
      } yield {
        arns must be_==(arbCluster.map(tuple => tuple._1.clusterArn))
      }

    }

    "return all the container instances for the given cluster" >> prop { arbCluster: ArbitraryCluster =>
      val testClass = EcsAlg[IO](fakeECSAsync(arbCluster))

      (for {
        (expectedCluster, expectedContainerInstances) <- Stream.emits(arbCluster)
        containerInstances <- Stream.eval(testClass.listContainerInstances(expectedCluster.clusterArn).compile.toList)
      } yield {
        containerInstances must be_==(expectedContainerInstances)
      }).map(_.toResult).compile.foldMonoid

    }

    "return the matching container instance when asked to search by EC2 Instance ID" >> prop { arbCluster: ArbitraryCluster =>
      val testClass = EcsAlg[IO](fakeECSAsync(arbCluster))
      val (expectedCluster, expectedCI) = arbCluster.find(_._2.nonEmpty).flatMap {
        case (c, cis) => cis.lastOption.map(c -> _)
      }.getOrElse(throw new RuntimeException("test setup exception; no container instances were found in the arbitrary cluster"))

      for {
        containerInstance <- testClass.findEc2Instance(expectedCI.ec2InstanceId)
      } yield {
        containerInstance must beSome[(ClusterArn, ContainerInstance)].like {
          case (actualCluster, actualCI) =>
            actualCluster must be_==(expectedCluster.clusterArn)
            actualCI must be_==(expectedCI)
        }
      }
    }

    "update the given instance's status to Draining if it's not already" >> prop { (cluster: ClusterArn, ci: ContainerInstance) =>
      val activeContainerInstance = ci.copy(status = ContainerStatus.Active)

      for {
        deferredRequest <- Deferred[IO, UpdateContainerInstancesStateRequest]
        fakeEcsClient <- IO.asyncF[AmazonECSAsync] { completeMock =>
          IO.async[UpdateContainerInstancesStateRequest] { completeReq =>
            completeMock(Right(new FakeECSAsync {
              override def updateContainerInstancesStateAsync(req: UpdateContainerInstancesStateRequest,
                                                              asyncHandler: AsyncHandler[UpdateContainerInstancesStateRequest, UpdateContainerInstancesStateResult]): Future[UpdateContainerInstancesStateResult] = {
                completeReq(Right(req))
                asyncHandler.onSuccess(req, new UpdateContainerInstancesStateResult())
                null
              }
            }))
          }.flatMap(deferredRequest.complete)
        }
        _ <- EcsAlg[IO](fakeEcsClient).drainInstance(cluster, activeContainerInstance)
        completedRequest <- deferredRequest.get
      } yield {
        completedRequest.getCluster must be_==(cluster)
        completedRequest.getContainerInstances.asScala must contain(activeContainerInstance.containerInstanceId)
        completedRequest.getStatus must  be_==(Draining.toString)
      }
    }

    "ignore requests to change the status of instances that are already draining" >> prop { (cluster: ClusterArn, ci: ContainerInstance) =>
      val activeContainerInstance = ci.copy(status = ContainerStatus.Draining)

      EcsAlg[IO](new FakeECSAsync {}).drainInstance(cluster, activeContainerInstance) must returnOk[Unit]
    }
  }
}
