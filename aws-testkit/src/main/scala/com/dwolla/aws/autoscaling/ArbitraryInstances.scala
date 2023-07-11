package com.dwolla.aws.autoscaling

import cats.syntax.all.*
import java.time.*
import com.dwolla.aws.AccountId
import com.dwolla.aws.ArbitraryInstances.*
import com.dwolla.aws.ec2.Ec2InstanceId
import com.fortysevendeg.scalacheck.datetime.jdk8.ArbitraryJdk8.*
import org.scalacheck.Arbitrary.{arbUuid, arbitrary}
import org.scalacheck.*
import LifecycleState.*
import software.amazon.awssdk.services.autoscaling.model.{DescribeAutoScalingInstancesResponse, AutoScalingInstanceDetails}

object ArbitraryInstances extends ArbitraryInstances

trait ArbitraryInstances {
  // TODO genAutoScalingGroupName could be more realistic
  val genAutoScalingGroupName: Gen[AutoScalingGroupName] = Gen.asciiStr.map(AutoScalingGroupName(_))
  implicit val arbAutoScalingGroupName: Arbitrary[AutoScalingGroupName] = Arbitrary(genAutoScalingGroupName)

  val genLifecycleHookNotification: Gen[LifecycleHookNotification] =
    for {
      service <- Gen.const("AWS Auto Scaling")
      time <- arbitrary[ZonedDateTime].map(_.toInstant)
      requestId <- arbUuid.arbitrary.map(_.toString)
      lifecycleActionToken <- arbUuid.arbitrary.map(_.toString)
      accountId <- arbitrary[AccountId]
      groupName <- arbitrary[AutoScalingGroupName]
      hookName <- Gen.asciiStr.map(LifecycleHookName(_)) // TODO move to its own Arbitrary
      ec2InstanceId <- arbitrary[Ec2InstanceId]
      lifecycleTransition <- Gen.const("autoscaling:EC2_INSTANCE_TERMINATING").map(LifecycleTransition(_)) // TODO move to its own Arbitrary
    } yield LifecycleHookNotification(service, time, requestId, lifecycleActionToken, accountId, groupName, hookName, ec2InstanceId, lifecycleTransition, None)
  implicit val arbLifecycleHookNotification: Arbitrary[LifecycleHookNotification] = Arbitrary(genLifecycleHookNotification)

  val genLifecycleState: Gen[LifecycleState] =
    Gen.frequency(
      10 -> PendingWait,
      1 -> PendingProceed,
      10 -> InService,
      10 -> TerminatingWait,
      1 -> TerminatingProceed,
    )
  implicit val arbLifecycleState: Arbitrary[LifecycleState] = Arbitrary(genLifecycleState)

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
  implicit val arbLifecycleHookNotificationWithRelatedDescribeAutoScalingInstancesResponse: Arbitrary[(LifecycleHookNotification, DescribeAutoScalingInstancesResponse)] =
    Arbitrary(genLifecycleHookNotificationWithRelatedDescribeAutoScalingInstancesResponse)
}

extension [A](maybeA: Option[A]) {
  def orGen(using Arbitrary[A]): Gen[A] =
    maybeA.fold(arbitrary[A])(Gen.const)
}
