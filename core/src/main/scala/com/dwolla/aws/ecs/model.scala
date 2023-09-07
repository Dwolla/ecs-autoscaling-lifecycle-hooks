package com.dwolla.aws.ecs

import cats.Order
import monix.newtypes.*
import cats.syntax.all.*
import com.dwolla.aws.AccountId
import com.dwolla.aws.ec2.Ec2InstanceId

type ContainerInstanceId = ContainerInstanceId.Type
object ContainerInstanceId extends NewtypeWrapped[String]
type ClusterArn = ClusterArn.Type
object ClusterArn extends NewtypeWrapped[String]
type ClusterName = ClusterName.Type
object ClusterName extends NewtypeWrapped[String]
type TaskCount = TaskCount.Type
object TaskCount extends NewtypeWrapped[Int] {
  given Order[TaskCount] = Order[Int].contramap(_.value)
}
type Region = Region.Type
object Region extends NewtypeWrapped[String]

case class Cluster(region: Region, accountId: AccountId, name: ClusterName) {
  val clusterArn: ClusterArn = ClusterArn(s"arn:aws:ecs:$region:$accountId:cluster/$name")
}

case class ContainerInstance(containerInstanceId: ContainerInstanceId,
                             ec2InstanceId: Ec2InstanceId,
                             runningTaskCount: TaskCount,
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
  case Pending
  case Running
  case Stopped

  override def toString: String = this match {
    case Pending => "PENDING"
    case Running => "RUNNING"
    case Stopped => "STOPPED"
  }
}
object TaskStatus {
  def fromString(s: String): Option[TaskStatus] =
    s match {
      case "PENDING" => TaskStatus.Pending.some
      case "RUNNING" => TaskStatus.Running.some
      case "STOPPED" => TaskStatus.Stopped.some
    }
}
