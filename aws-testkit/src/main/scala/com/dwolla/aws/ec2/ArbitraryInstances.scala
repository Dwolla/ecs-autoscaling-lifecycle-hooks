package com.dwolla.aws.ec2

import com.amazonaws.ec2.{Instance, InstanceId, Tag as AwsTag}
import org.scalacheck.*
import org.scalacheck.Arbitrary.arbitrary

given Arbitrary[InstanceId] =
  Arbitrary(Gen.listOfN(17, Gen.hexChar).map(_.mkString("i-", "", "")).map(InstanceId(_)))

val genAwsTag: Gen[AwsTag] =
  for {
    key <- Gen.option(Gen.identifier)
    value <- Gen.option(arbitrary[String])
  } yield AwsTag(key, value)

val genInstance: Gen[Instance] =
  for {
    id <- Gen.option(arbitrary[InstanceId])
    tags <- Gen.option(Gen.nonEmptyListOf(genAwsTag))
  } yield Instance(instanceId = id.map(_.value), tags = tags)
given Arbitrary[Instance] = Arbitrary(genInstance)
