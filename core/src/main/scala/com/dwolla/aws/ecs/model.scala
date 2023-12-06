package com.dwolla.aws.ecs

import cats.syntax.all.*
import cats.tagless.aop.Aspect
import cats.{Eval, Order, ~>}
import com.amazonaws.ec2.InstanceId
import com.amazonaws.ecs
import com.amazonaws.ecs.*
import com.dwolla.aws.TraceableValueInstances.given
import com.dwolla.aws.{AccountId, TraceableValueInstances, given}
import io.circe.JsonObject
import monix.newtypes.*
import natchez.TraceableValue
import smithy4s.aws.AwsRegion
import io.circe.syntax.*

type ContainerInstanceId = ContainerInstanceId.Type
object ContainerInstanceId extends NewtypeWrapped[String]
type ClusterArn = ClusterArn.Type
object ClusterArn extends NewtypeWrapped[String] {
  def unapply(arg: Cluster): Some[ClusterArn] = Some(arg.clusterArn)
}
type ClusterName = ClusterName.Type
object ClusterName extends NewtypeWrapped[String]
type TaskCount = TaskCount.Type
object TaskCount extends NewtypeWrapped[Long] {
  given Order[TaskCount] = Order[Long].contramap(_.value)
}

type CQLQuery = CQLQuery.Type
object CQLQuery extends NewtypeWrapped[String]

case class Cluster(region: AwsRegion, accountId: AccountId, name: ClusterName) {
  val clusterArn: ClusterArn = ClusterArn(s"arn:aws:ecs:$region:$accountId:cluster/$name")
}

case class ContainerInstance(containerInstanceId: ContainerInstanceId,
                             ec2InstanceId: InstanceId,
                             countOfTasksNotStopped: TaskCount,
                             status: ContainerInstanceStatus,
                            )
object ContainerInstance {
  given TraceableValue[ContainerInstance] =
    TraceableValue.stringToTraceValue.contramap { ci =>
      JsonObject(
        "containerInstanceId" -> ci.containerInstanceId.value.asJson,
        "ec2InstanceId" -> ci.ec2InstanceId.value.asJson,
        "countOfTasksNotStopped" -> ci.countOfTasksNotStopped.value.asJson,
        "status" -> ci.status.toString.asJson,
      ).asJson.noSpaces
    }
}

enum ContainerInstanceStatus {
  case Active
  case Draining
  case Inactive

  override def toString: String = this match {
    case Active => "ACTIVE"
    case Draining => "DRAINING"
    case Inactive => "INACTIVE"
  }
}

object ContainerInstanceStatus {
  def fromStatus(s: String): Option[ContainerInstanceStatus] = s match {
    case "ACTIVE" => Active.some
    case "INACTIVE" => Inactive.some
    case "DRAINING" =>  Draining.some
    case _ => None
  }
}

type TaskArn = TaskArn.Type
object TaskArn extends NewtypeWrapped[String]

type TaskDefinitionArn = TaskDefinitionArn.Type
object TaskDefinitionArn extends NewtypeWrapped[String]

enum TaskStatus {
  case Provisioning
  case Pending
  case Activating
  case Running
  case Deactivating
  case Stopping
  case Deprovisioning
  case Stopped
  case Deleted

  override def toString: String = this match {
    case Provisioning => "PROVISIONING" 
    case Pending => "PENDING" 
    case Activating => "ACTIVATING" 
    case Running => "RUNNING" 
    case Deactivating => "DEACTIVATING" 
    case Stopping => "STOPPING" 
    case Deprovisioning => "DEPROVISIONING" 
    case Stopped => "STOPPED" 
    case Deleted => "DELETED"   
  }
}
object TaskStatus {
  val stoppedTaskStatuses: Set[TaskStatus] = Set(Stopped, Deleted)

  def fromString: PartialFunction[String, TaskStatus] = {
    case "PROVISIONING" => Provisioning
    case "PENDING" => Pending
    case "ACTIVATING" => Activating
    case "RUNNING" => Running
    case "DEACTIVATING" => Deactivating
    case "STOPPING" => Stopping
    case "DEPROVISIONING" => Deprovisioning
    case "STOPPED" => Stopped
    case "DELETED" => Deleted
  }
}

def traceableAdvice[A: TraceableValue](name: String, a: A): Aspect.Advice[Eval, TraceableValue] =
  Aspect.Advice.byValue[TraceableValue, A](name, a)

given Aspect[ECS, TraceableValue, TraceableValue] =
  new Aspect[ECS, TraceableValue, TraceableValue] {
    override def weave[F[_]](af: ECS[F]): ECS[[A] =>> Aspect.Weave[F, TraceableValue, TraceableValue, A]] =
      new ECS[[A] =>> Aspect.Weave[F, TraceableValue, TraceableValue, A]] {
        override def describeContainerInstances(containerInstances: List[String],
                                                cluster: Option[String],
                                                include: Option[List[ContainerInstanceField]]): Aspect.Weave[F, TraceableValue, TraceableValue, DescribeContainerInstancesResponse] =
          Aspect.Weave[F, TraceableValue, TraceableValue, DescribeContainerInstancesResponse](
            "ECS",
            List(List(
              traceableAdvice("containerInstances", containerInstances),
              traceableAdvice("cluster", cluster),
              traceableAdvice("include", include),
            )),
            Aspect.Advice[F, TraceableValue, DescribeContainerInstancesResponse](
              "describeContainerInstances",
              af.describeContainerInstances(containerInstances, cluster, include)
            )
          )

        override def describeTasks(tasks: List[String],
                                   cluster: Option[String],
                                   include: Option[List[TaskField]]): Aspect.Weave[F, TraceableValue, TraceableValue, DescribeTasksResponse] =
          Aspect.Weave[F, TraceableValue, TraceableValue, DescribeTasksResponse](
            "ECS",
            List(List(
              traceableAdvice("tasks", tasks),
              traceableAdvice("cluster", cluster),
              traceableAdvice("include", include),
            )),
            Aspect.Advice[F, TraceableValue, DescribeTasksResponse](
              "describeTasks",
              af.describeTasks(tasks, cluster, include)
            )
          )

        override def listClusters(nextToken: Option[String],
                                  maxResults: Option[BoxedInteger]): Aspect.Weave[F, TraceableValue, TraceableValue, ListClustersResponse] =
          Aspect.Weave[F, TraceableValue, TraceableValue, ListClustersResponse](
            "ECS",
            List(List(
              traceableAdvice("nextToken", nextToken),
              traceableAdvice("maxResults", maxResults),
            )),
            Aspect.Advice[F, TraceableValue, ListClustersResponse](
              "listClusters",
              af.listClusters(nextToken, maxResults)
            )
          )

        override def listContainerInstances(cluster: Option[String],
                                            filter: Option[String],
                                            nextToken: Option[String],
                                            maxResults: Option[BoxedInteger],
                                            status: Option[ecs.ContainerInstanceStatus]): Aspect.Weave[F, TraceableValue, TraceableValue, ListContainerInstancesResponse] =
          Aspect.Weave[F, TraceableValue, TraceableValue, ListContainerInstancesResponse](
            "ECS",
            List(List(
              traceableAdvice("cluster", cluster),
              traceableAdvice("filter", filter),
              traceableAdvice("nextToken", nextToken),
              traceableAdvice("maxResults", maxResults),
              traceableAdvice("status", status),
            )),
            Aspect.Advice[F, TraceableValue, ListContainerInstancesResponse](
              "listContainerInstances",
              af.listContainerInstances(cluster, filter, nextToken, maxResults, status)
            )
          )

        override def listTasks(cluster: Option[String],
                               containerInstance: Option[String],
                               family: Option[String],
                               nextToken: Option[String],
                               maxResults: Option[BoxedInteger],
                               startedBy: Option[String],
                               serviceName: Option[String],
                               desiredStatus: Option[DesiredStatus],
                               launchType: Option[LaunchType]): Aspect.Weave[F, TraceableValue, TraceableValue, ListTasksResponse] =
          Aspect.Weave[F, TraceableValue, TraceableValue, ListTasksResponse](
            "ECS",
            List(List(
              traceableAdvice("cluster", cluster),
              traceableAdvice("containerInstance", containerInstance),
              traceableAdvice("family", family),
              traceableAdvice("nextToken", nextToken),
              traceableAdvice("maxResults", maxResults),
              traceableAdvice("startedBy", startedBy),
              traceableAdvice("serviceName", serviceName),
              traceableAdvice("desiredStatus", desiredStatus),
              traceableAdvice("launchType", launchType),
            )),
            Aspect.Advice[F, TraceableValue, ListTasksResponse](
              "listTasks",
              af.listTasks(cluster, containerInstance, family, nextToken, maxResults, startedBy, serviceName, desiredStatus, launchType)
            )
          )

        override def updateContainerInstancesState(containerInstances: List[String],
                                                   status: ecs.ContainerInstanceStatus,
                                                   cluster: Option[String]): Aspect.Weave[F, TraceableValue, TraceableValue, UpdateContainerInstancesStateResponse] =
          Aspect.Weave[F, TraceableValue, TraceableValue, UpdateContainerInstancesStateResponse](
            "ECS",
            List(List(
              traceableAdvice("containerInstances", containerInstances),
              traceableAdvice("status", status),
              traceableAdvice("cluster", cluster),
            )),
            Aspect.Advice[F, TraceableValue, UpdateContainerInstancesStateResponse](
              "updateContainerInstancesState",
              af.updateContainerInstancesState(containerInstances, status, cluster)
            )
          )
      }

    override def mapK[F[_], G[_]](af: ECS[F])(fk: F ~> G): ECS[G] =
      new ECS[G] {
        override def describeContainerInstances(containerInstances: List[String],
                                                cluster: Option[String],
                                                include: Option[List[ContainerInstanceField]]): G[DescribeContainerInstancesResponse] =
          fk(af.describeContainerInstances(containerInstances, cluster, include))

        override def describeTasks(tasks: List[String],
                                   cluster: Option[String],
                                   include: Option[List[TaskField]]): G[DescribeTasksResponse] =
          fk(af.describeTasks(tasks, cluster, include))

        override def listClusters(nextToken: Option[String],
                                  maxResults: Option[BoxedInteger]): G[ListClustersResponse] =
          fk(af.listClusters(nextToken, maxResults))

        override def listContainerInstances(cluster: Option[String],
                                            filter: Option[String],
                                            nextToken: Option[String],
                                            maxResults: Option[BoxedInteger],
                                            status: Option[ecs.ContainerInstanceStatus]): G[ListContainerInstancesResponse] =
          fk(af.listContainerInstances(cluster, filter, nextToken, maxResults, status))

        override def listTasks(cluster: Option[String],
                               containerInstance: Option[String],
                               family: Option[String],
                               nextToken: Option[String],
                               maxResults: Option[BoxedInteger],
                               startedBy: Option[String],
                               serviceName: Option[String],
                               desiredStatus: Option[DesiredStatus],
                               launchType: Option[LaunchType]): G[ListTasksResponse] =
          fk(af.listTasks(cluster, containerInstance, family, nextToken, maxResults, startedBy, serviceName, desiredStatus, launchType))

        override def updateContainerInstancesState(containerInstances: List[String],
                                                   status: ecs.ContainerInstanceStatus,
                                                   cluster: Option[String]): G[UpdateContainerInstancesStateResponse] =
          fk(af.updateContainerInstancesState(containerInstances, status, cluster))
      }
  }
