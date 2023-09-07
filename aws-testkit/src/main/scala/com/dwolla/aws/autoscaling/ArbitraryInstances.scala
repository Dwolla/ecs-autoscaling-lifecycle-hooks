package com.dwolla.aws.autoscaling

import cats.syntax.all.*
import java.time.*
import com.dwolla.aws.AccountId
import com.dwolla.aws.ec2.Ec2InstanceId
import com.fortysevendeg.scalacheck.datetime.jdk8.ArbitraryJdk8.*
import org.scalacheck.Arbitrary.{arbUuid, arbitrary}
import org.scalacheck.*
import LifecycleState.*
import software.amazon.awssdk.services.autoscaling.model.{DescribeAutoScalingInstancesResponse, AutoScalingInstanceDetails}
import com.dwolla.aws.given 
import com.dwolla.aws.ec2.given

// TODO genAutoScalingGroupName could be more realistic
val genAutoScalingGroupName: Gen[AutoScalingGroupName] = Gen.asciiStr.map(AutoScalingGroupName(_))
given Arbitrary[AutoScalingGroupName] = Arbitrary(genAutoScalingGroupName)

val genLifecycleHookName: Gen[LifecycleHookName] = Gen.asciiStr.map(LifecycleHookName(_))
given Arbitrary[LifecycleHookName] = Arbitrary(genLifecycleHookName)

val genLifecycleTransition: Gen[LifecycleTransition] = Gen.const("autoscaling:EC2_INSTANCE_TERMINATING").map(LifecycleTransition(_))
given Arbitrary[LifecycleTransition] = Arbitrary(genLifecycleTransition)

val genLifecycleHookNotification: Gen[LifecycleHookNotification] =
  for {
    service <- Gen.const("AWS Auto Scaling")
    time <- arbitrary[ZonedDateTime].map(_.toInstant)
    requestId <- arbUuid.arbitrary.map(_.toString)
    lifecycleActionToken <- arbUuid.arbitrary.map(_.toString)
    accountId <- arbitrary[AccountId]
    groupName <- arbitrary[AutoScalingGroupName]
    hookName <- arbitrary[LifecycleHookName]
    ec2InstanceId <- arbitrary[Ec2InstanceId]
    lifecycleTransition <- arbitrary[LifecycleTransition]
  } yield LifecycleHookNotification(service, time, requestId, lifecycleActionToken, accountId, groupName, hookName, ec2InstanceId, lifecycleTransition, None)
given Arbitrary[LifecycleHookNotification] = Arbitrary(genLifecycleHookNotification)

val genLifecycleState: Gen[LifecycleState] =
  Gen.frequency(
    10 -> PendingWait,
    1 -> PendingProceed,
    10 -> InService,
    10 -> TerminatingWait,
    1 -> TerminatingProceed,
  )
given Arbitrary[LifecycleState] = Arbitrary(genLifecycleState)

def genAutoScalingInstanceDetails(maybeId: Option[Ec2InstanceId] = None,
                                  maybeAutoScalingGroupName: Option[AutoScalingGroupName] = None,
                                  maybeLifecycleState: Option[LifecycleState] = None,
                                 ): Gen[AutoScalingInstanceDetails] =
  for {
    id <- maybeId.orGen
    asgName <- maybeAutoScalingGroupName.orGen
    lifecycleState <- maybeLifecycleState.orGen
  } yield
    AutoScalingInstanceDetails
      .builder()
      .instanceId(id.value)
      .autoScalingGroupName(asgName.value)
      .lifecycleState(lifecycleState.awsName)
      .build()

val genLifecycleHookNotificationWithRelatedDescribeAutoScalingInstancesResponse: Gen[(LifecycleHookNotification, DescribeAutoScalingInstancesResponse)] =
  for {
    notification <- genLifecycleHookNotification
    groupName <- arbitrary[AutoScalingGroupName]
    autoScalingDetailsFromHook <- genAutoScalingInstanceDetails(notification.EC2InstanceId.some, groupName.some)
    otherAutoScalingDetails <- Gen.listOf(genAutoScalingInstanceDetails(maybeAutoScalingGroupName = groupName.some))
  } yield {
    notification -> DescribeAutoScalingInstancesResponse.builder()
      .autoScalingInstances(otherAutoScalingDetails.appended(autoScalingDetailsFromHook) *)
      .build()
  }
given Arbitrary[(LifecycleHookNotification, DescribeAutoScalingInstancesResponse)] =
  Arbitrary(genLifecycleHookNotificationWithRelatedDescribeAutoScalingInstancesResponse)

extension [A](maybeA: Option[A]) {
  def orGen(using Arbitrary[A]): Gen[A] =
    maybeA.fold(arbitrary[A])(Gen.const)
}
