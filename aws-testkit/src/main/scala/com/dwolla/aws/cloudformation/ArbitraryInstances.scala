package com.dwolla.aws.cloudformation

import com.amazonaws.cloudformation.*
import org.scalacheck.{Arbitrary, Gen}

val genStackArn: Gen[StackArn] =
  for {
    region <- Gen.oneOf("us-west-2", "us-east-1")
    accountId <- Gen.listOfN(12, Gen.numChar).map(_.mkString)
    name <- Gen.identifier
    id <- Gen.uuid
  } yield StackArn(s"arn:aws:cloudformation:$region:$accountId:stack/$name/$id")
given Arbitrary[StackArn] = Arbitrary(genStackArn)

val genLogicalResourceId: Gen[LogicalResourceId] = Gen.identifier.map(LogicalResourceId(_))
given Arbitrary[LogicalResourceId] = Arbitrary(genLogicalResourceId)

val genPhysicalResourceId: Gen[PhysicalResourceId] = Gen.asciiPrintableStr.map(PhysicalResourceId(_))
given Arbitrary[PhysicalResourceId] = Arbitrary(genPhysicalResourceId)

val genResourceType: Gen[ResourceType] = Gen.asciiPrintableStr.map(ResourceType.apply)
given Arbitrary[ResourceType] = Arbitrary(genResourceType)

val genResourceStatus: Gen[ResourceStatus] = Gen.oneOf(ResourceStatus.values)
given Arbitrary[ResourceStatus] = Arbitrary(genResourceStatus)
