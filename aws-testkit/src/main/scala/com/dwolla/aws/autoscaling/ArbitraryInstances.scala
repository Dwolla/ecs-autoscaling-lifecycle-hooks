package com.dwolla.aws.autoscaling

import cats.syntax.all.*
import com.amazonaws.autoscaling.{LifecycleState as _, *}
import com.amazonaws.ec2.InstanceId
import com.dwolla.aws.autoscaling.LifecycleState.*
import com.dwolla.aws.ec2.given
import com.dwolla.aws.{AccountId, given}
import com.fortysevendeg.scalacheck.datetime.jdk8.ArbitraryJdk8.*
import org.scalacheck.*
import org.scalacheck.Arbitrary.{arbChar, arbUuid, arbitrary}

import java.time.*

given Arbitrary[ResourceName] = Arbitrary(Gen.asciiStr.map(ResourceName.apply))
given Arbitrary[AsciiStringMaxLen255] = Arbitrary {
  Gen.chooseNum(0, 255)
    .flatMap(Gen.stringOfN(_, Gen.asciiChar))
    .map(AsciiStringMaxLen255.apply)
}

// TODO genAutoScalingGroupName could be more realistic
val genAutoScalingGroupName: Gen[AutoScalingGroupName] = arbitrary[ResourceName].map(AutoScalingGroupName(_))
given Arbitrary[AutoScalingGroupName] = Arbitrary(genAutoScalingGroupName)

val genLifecycleHookName: Gen[LifecycleHookName] = arbitrary[AsciiStringMaxLen255].map(LifecycleHookName(_))
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
    InstanceId <- arbitrary[InstanceId]
    lifecycleTransition <- arbitrary[LifecycleTransition]
  } yield LifecycleHookNotification(service, time, requestId, lifecycleActionToken, accountId, groupName, hookName, InstanceId, lifecycleTransition, None)
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

given Arbitrary[XmlStringMaxLen255] = Arbitrary {
  Gen.chooseNum(0, 255)
    .flatMap(Gen.stringOfN(_, arbitrary[Char]))
    .map(XmlStringMaxLen255.apply)
}
given Arbitrary[XmlStringMaxLen32] = Arbitrary {
  Gen.chooseNum(0, 32)
    .flatMap(Gen.stringOfN(_, arbitrary[Char]))
    .map(XmlStringMaxLen32.apply)
}
given Arbitrary[InstanceProtected] = Arbitrary(arbitrary[Boolean].map(InstanceProtected.apply))

def genAutoScalingInstanceDetails(maybeId: Option[InstanceId] = None,
                                  maybeAutoScalingGroupName: Option[AutoScalingGroupName] = None,
                                  maybeLifecycleState: Option[LifecycleState] = None,
                                 ): Gen[AutoScalingInstanceDetails] =
  for {
    id <- maybeId.orGen
    asgName <- maybeAutoScalingGroupName.orGen
    lifecycleState <- maybeLifecycleState.orGen
    availabilityZone <- arbitrary[XmlStringMaxLen255]
    healthStatus <- arbitrary[XmlStringMaxLen32]
    protectedFromScaleIn <- arbitrary[InstanceProtected]
    instanceType <- Gen.option(arbitrary[XmlStringMaxLen255])
    launchConfigurationName = None
    launchTemplate = None
    weightedCapacity = None
  } yield
    AutoScalingInstanceDetails(
      instanceId = XmlStringMaxLen19(id.value),
      autoScalingGroupName = XmlStringMaxLen255(asgName.value.value),
      lifecycleState = XmlStringMaxLen32(lifecycleState.awsName),
      availabilityZone = availabilityZone,
      healthStatus = healthStatus,
      protectedFromScaleIn = protectedFromScaleIn,
      instanceType = instanceType,
      launchConfigurationName = launchConfigurationName,
      launchTemplate = launchTemplate,
      weightedCapacity = weightedCapacity,
    )

val genLifecycleHookNotificationWithRelatedAutoScalingInstancesType: Gen[(LifecycleHookNotification, AutoScalingInstancesType)] =
  for {
    notification <- genLifecycleHookNotification
    groupName <- arbitrary[AutoScalingGroupName]
    autoScalingDetailsFromHook <- genAutoScalingInstanceDetails(notification.EC2InstanceId.some, groupName.some)
    otherAutoScalingDetails <- Gen.listOf(genAutoScalingInstanceDetails(maybeAutoScalingGroupName = groupName.some))
    details = otherAutoScalingDetails.appended(autoScalingDetailsFromHook)
  } yield notification -> AutoScalingInstancesType(details.some) 
given Arbitrary[(LifecycleHookNotification, AutoScalingInstancesType)] =
  Arbitrary(genLifecycleHookNotificationWithRelatedAutoScalingInstancesType)

extension [A](maybeA: Option[A]) {
  def orGen(using Arbitrary[A]): Gen[A] =
    maybeA.fold(arbitrary[A])(Gen.const)
}
