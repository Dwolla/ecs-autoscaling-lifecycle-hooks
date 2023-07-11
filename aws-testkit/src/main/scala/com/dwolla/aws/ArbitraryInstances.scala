package com.dwolla.aws

import org.scalacheck.*

object ArbitraryInstances extends ArbitraryInstances

trait ArbitraryInstances
  extends autoscaling.ArbitraryInstances
    with ecs.ArbitraryInstances
    with ec2.ArbitraryInstances
    with sns.ArbitraryInstances
    with cloudformation.ArbitraryInstances {

  implicit val arbAccountId: Arbitrary[AccountId] =
    Arbitrary(Gen.listOfN(12, Gen.numChar).map(_.mkString).map(AccountId(_)))

  val genTag: Gen[Tag] =
    for {
      key <- Gen.asciiPrintableStr.map(TagName(_))
      value <- Gen.asciiPrintableStr.map(TagValue(_))
    } yield Tag(key, value)
  implicit val arbTag: Arbitrary[Tag] = Arbitrary(genTag)
}
