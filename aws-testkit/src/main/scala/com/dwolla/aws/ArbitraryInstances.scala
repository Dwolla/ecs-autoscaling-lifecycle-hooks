package com.dwolla.aws

import org.scalacheck.*

object ArbitraryInstances extends ArbitraryInstances

trait ArbitraryInstances
  extends autoscaling.model.ArbitraryInstances
    with ecs.model.ArbitraryInstances
    with ec2.model.ArbitraryInstances
    with sns.model.ArbitraryInstances {

  implicit val arbAccountId: Arbitrary[AccountId] =
    Arbitrary(Gen.listOfN(12, Gen.numChar).map(_.mkString).map(AccountId(_)))

}
