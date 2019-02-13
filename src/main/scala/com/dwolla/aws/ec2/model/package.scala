package com.dwolla.aws.ec2

import shapeless.tag._
import shapeless.tag

package object model {
  type Ec2InstanceId = String @@ Ec2InstanceIdTag

  val tagEc2InstanceId: String => Ec2InstanceId = tag[Ec2InstanceIdTag][String]
}

package model {
  trait Ec2InstanceIdTag
}
