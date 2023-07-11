package com.dwolla.aws.cloudformation

import org.scalacheck.{Arbitrary, Gen}

object ArbitraryInstances extends ArbitraryInstances

trait ArbitraryInstances {
  val genStackArn: Gen[StackArn] =
    for {
      region <- Gen.oneOf("us-west-2", "us-east-1")
      accountId <- Gen.listOfN(12, Gen.numChar).map(_.mkString)
      name <- Gen.identifier
      id <- Gen.uuid
    } yield StackArn(s"arn:aws:cloudformation:$region:$accountId:stack/$name/$id")
  implicit val arbStackArn: Arbitrary[StackArn] = Arbitrary(genStackArn)

  val genLogicalResourceId: Gen[LogicalResourceId] = Gen.identifier.map(LogicalResourceId(_))
  implicit val arbLogicalResourceId: Arbitrary[LogicalResourceId] = Arbitrary(genLogicalResourceId)

  val genPhysicalResourceId: Gen[PhysicalResourceId] = Gen.asciiPrintableStr.map(PhysicalResourceId(_))
  implicit val arbPhysicalResourceId: Arbitrary[PhysicalResourceId] = Arbitrary(genPhysicalResourceId)
}
