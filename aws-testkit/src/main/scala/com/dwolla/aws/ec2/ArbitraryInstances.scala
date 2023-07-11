package com.dwolla.aws.ec2

import org.scalacheck.*
import org.scalacheck.Arbitrary.arbitrary
import software.amazon.awssdk.services.ec2.model.{Instance, Tag as AwsTag}

object ArbitraryInstances extends ArbitraryInstances

trait ArbitraryInstances {
  implicit val arbEc2InstanceId: Arbitrary[Ec2InstanceId] = Arbitrary(Gen.listOfN(17, Gen.hexChar).map(_.mkString("i-", "", "")).map(Ec2InstanceId(_)))

  val genAwsTag: Gen[AwsTag] =
    for {
      key <- Gen.identifier
      value <- arbitrary[String]
    } yield AwsTag.builder().key(key).value(value).build()

  val genInstance: Gen[Instance] =
    for {
      id <- arbitrary[Ec2InstanceId]
      tags <- Gen.nonEmptyListOf(genAwsTag)
    } yield {
      Instance
        .builder()
        .instanceId(id.value)
        .tags(tags *)
        .build()
    }
  implicit val arbInstance: Arbitrary[Instance] = Arbitrary(genInstance)
}
