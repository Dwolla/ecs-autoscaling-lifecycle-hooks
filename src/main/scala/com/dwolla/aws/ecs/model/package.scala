package com.dwolla.aws.ecs

import shapeless.tag._
import shapeless.tag

package object model {
  type ContainerInstanceId = String @@ ContainerInstanceIdTag
  type ClusterArn = String @@ ClusterArnTag
  type ClusterName = String @@ ClusterNameTag
  type TaskCount = Int @@ TaskCountTag

  val tagContainerInstanceId: String => ContainerInstanceId = tag[ContainerInstanceIdTag][String]
  val tagClusterArn: String => ClusterArn = tag[ClusterArnTag][String]
  val tagClusterName: String => ClusterName = tag[ClusterNameTag][String]
  val tagTaskCount: Int => TaskCount = tag[TaskCountTag][Int]
}

package model {

  import com.amazonaws.regions.Regions
  import com.dwolla.aws.AccountId
  import com.dwolla.aws.ec2.model.Ec2InstanceId

  trait ContainerInstanceIdTag
  trait ClusterNameTag
  trait ClusterArnTag
  trait TaskCountTag

  case class Cluster(region: Regions, accountId: AccountId, name: ClusterName) {
    val clusterArn: ClusterArn = tagClusterArn(s"arn:aws:ecs:${region.name()}:$accountId:cluster/$name")
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

    def fromStatus(s: String): ContainerStatus = s match {
      case "ACTIVE" => Active
      case "INACTIVE" => Inactive
      case "DRAINING" =>  Draining
    }
  }
}
