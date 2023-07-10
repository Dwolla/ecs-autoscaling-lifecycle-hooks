package com.dwolla.aws.autoscaling

import java.time.*

import com.dwolla.aws.AccountId
import com.dwolla.aws.ArbitraryInstances.*
import com.dwolla.aws.ec2.Ec2InstanceId
import com.fortysevendeg.scalacheck.datetime.jdk8.ArbitraryJdk8.*
import org.scalacheck.Arbitrary.{arbitrary, arbUuid}
import org.scalacheck.*

object ArbitraryInstances extends ArbitraryInstances

trait ArbitraryInstances {
  val genLifecycleHookNotification: Gen[LifecycleHookNotification] =
    for {
      service <- Gen.const("AWS Auto Scaling")
      time <- arbitrary[ZonedDateTime].map(_.toInstant)
      requestId <- arbUuid.arbitrary.map(_.toString)
      lifecycleActionToken <- arbUuid.arbitrary.map(_.toString)
      accountId <- arbitrary[AccountId]
      groupName <- Gen.asciiStr
      hookName <- Gen.asciiStr
      ec2InstanceId <- arbitrary[Ec2InstanceId]
      lifecycleTransition <- Gen.const("autoscaling:EC2_INSTANCE_TERMINATING")
    } yield LifecycleHookNotification(service, time, requestId, lifecycleActionToken, accountId, groupName, hookName, ec2InstanceId, lifecycleTransition, None)
  implicit val arbLifecycleHookNotification: Arbitrary[LifecycleHookNotification] = Arbitrary(genLifecycleHookNotification)
}
