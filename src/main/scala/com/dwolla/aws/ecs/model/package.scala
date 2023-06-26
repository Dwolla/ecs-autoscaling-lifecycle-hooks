package com.dwolla.aws.ecs.model

import cats.Order
import monix.newtypes.*
import cats.syntax.all.*
import com.dwolla.aws.AccountId
import com.dwolla.aws.ec2.model.Ec2InstanceId

type ContainerInstanceId = ContainerInstanceId.Type
object ContainerInstanceId extends NewtypeWrapped[String]
type ClusterArn = ClusterArn.Type
object ClusterArn extends NewtypeWrapped[String]
type ClusterName = ClusterName.Type
object ClusterName extends NewtypeWrapped[String]
type TaskCount = TaskCount.Type
object TaskCount extends NewtypeWrapped[Int] {
  implicit val order: Order[TaskCount] = Order[Int].contramap(_.value)
}
type Region = Region.Type
object Region extends NewtypeWrapped[String]

case class Cluster(region: Region, accountId: AccountId, name: ClusterName) {
  val clusterArn: ClusterArn = ClusterArn(s"arn:aws:ecs:$region:$accountId:cluster/$name")
}

case class ContainerInstance(containerInstanceId: ContainerInstanceId,
                             ec2InstanceId: Ec2InstanceId,
                             runningTaskCount: TaskCount,
                             status: ContainerStatus,
                            )

sealed trait ContainerStatus
object ContainerStatus {
  case object Active extends ContainerStatus {
    override def toString: String = "ACTIVE"
  }
  case object Draining extends ContainerStatus {
    override def toString: String = "DRAINING"
  }
  case object Inactive extends ContainerStatus {
    override def toString: String = "INACTIVE"
  }

  def fromStatus(s: String): Option[ContainerStatus] = s match {
    case "ACTIVE" => Active.some
    case "INACTIVE" => Inactive.some
    case "DRAINING" =>  Draining.some
    case _ => None
  }
}
