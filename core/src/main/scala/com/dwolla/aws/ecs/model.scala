package com.dwolla.aws.ecs

import cats.Order
import monix.newtypes.*
import cats.syntax.all.*
import com.dwolla.aws.AccountId
import com.dwolla.aws.ec2.Ec2InstanceId

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
type Region = Region.Type
object Region extends NewtypeWrapped[String]

case class Cluster(region: Region, accountId: AccountId, name: ClusterName) {
  val clusterArn: ClusterArn = ClusterArn(s"arn:aws:ecs:$region:$accountId:cluster/$name")
}

case class ContainerInstance(containerInstanceId: ContainerInstanceId,
                             ec2InstanceId: Ec2InstanceId,
                             countOfTasksNotStopped: TaskCount,
                             status: ContainerInstanceStatus,
                            )

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
