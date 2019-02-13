package com.dwolla.aws

import org.scalacheck._

object ArbitraryInstances extends ArbitraryInstances
  with autoscaling.model.ArbitraryInstances
  with ecs.model.ArbitraryInstances
  with ec2.model.ArbitraryInstances

trait ArbitraryInstances {

  implicit val arbAccountId: Arbitrary[AccountId] =
    Arbitrary(Gen.listOfN(12, Gen.numChar).map(_.mkString).map(tagAccountId))

}
