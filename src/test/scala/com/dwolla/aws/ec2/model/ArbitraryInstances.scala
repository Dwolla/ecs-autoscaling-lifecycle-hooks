package com.dwolla.aws.ec2.model

import org.scalacheck._

object ArbitraryInstances extends ArbitraryInstances

trait ArbitraryInstances {
  val genHexChar: Gen[Char] = Gen.frequency((1, Gen.numChar), (1, Gen.choose(97.toChar, 102.toChar)))
  implicit val arbEc2InstanceId: Arbitrary[Ec2InstanceId] = Arbitrary(Gen.listOfN(17, genHexChar).map(_.mkString("i-", "", "")).map(tagEc2InstanceId))
}
