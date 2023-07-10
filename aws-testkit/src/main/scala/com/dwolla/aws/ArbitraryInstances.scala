package com.dwolla.aws

import org.scalacheck.*

object ArbitraryInstances extends ArbitraryInstances

trait ArbitraryInstances
  extends autoscaling.ArbitraryInstances
    with ecs.ArbitraryInstances
    with ec2.ArbitraryInstances
    with sns.ArbitraryInstances {

  implicit val arbAccountId: Arbitrary[AccountId] =
    Arbitrary(Gen.listOfN(12, Gen.numChar).map(_.mkString).map(AccountId(_)))

}
