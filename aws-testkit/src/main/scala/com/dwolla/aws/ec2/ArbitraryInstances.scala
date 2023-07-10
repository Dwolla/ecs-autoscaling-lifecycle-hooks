package com.dwolla.aws.ec2

import org.scalacheck.*

object ArbitraryInstances extends ArbitraryInstances

trait ArbitraryInstances {
  implicit val arbEc2InstanceId: Arbitrary[Ec2InstanceId] = Arbitrary(Gen.listOfN(17, Gen.hexChar).map(_.mkString("i-", "", "")).map(Ec2InstanceId(_)))
}
