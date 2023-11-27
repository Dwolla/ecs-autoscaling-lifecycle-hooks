package com.dwolla.aws.autoscaling

import cats.effect.*
import cats.effect.testkit.TestControl
import cats.syntax.all.*
import com.amazonaws.autoscaling.{LifecycleState as _, *}
import com.amazonaws.ec2.InstanceId
import com.amazonaws.sns.{Message, TopicARN}
import com.dwolla.aws.autoscaling.LifecycleState.*
import com.dwolla.aws.autoscaling.given
import com.dwolla.aws.sns.{SnsAlg, given}
import io.circe.syntax.*
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.effect.PropF.forAllF
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.noop.NoOpFactory

import scala.concurrent.duration.*

class AutoScalingAlgImplSpec
  extends CatsEffectSuite
    with ScalaCheckEffectSuite {

  given LoggerFactory[IO] = NoOpFactory[IO]

  test("AutoScalingAlgImpl should make an async completeLifecycleAction request to continueAutoScaling") {
    forAllF { (arbLifecycleHookNotification: LifecycleHookNotification) =>
      for {
        deferredCompleteLifecycleActionRequest <- Deferred[IO, (AsciiStringMaxLen255, ResourceName, LifecycleActionResult, Option[XmlStringMaxLen19])]
        autoScalingClient = new AutoScaling.Default[IO](new NotImplementedError().raiseError) {
          override def completeLifecycleAction(lifecycleHookName: AsciiStringMaxLen255,
                                               autoScalingGroupName: ResourceName,
                                               lifecycleActionResult: LifecycleActionResult,
                                               lifecycleActionToken: Option[LifecycleActionToken],
                                               instanceId: Option[XmlStringMaxLen19]): IO[CompleteLifecycleActionAnswer] =
            deferredCompleteLifecycleActionRequest.complete((lifecycleHookName, autoScalingGroupName, lifecycleActionResult, instanceId))
              .as(CompleteLifecycleActionAnswer())
        }

        sns = new SnsAlg[IO] {
          override def publish(topic: TopicARN, message: Message): IO[Unit] =
            IO.raiseError(new RuntimeException("SnsAsyncClient.publish should not have been called"))
        }

        _ <- new AutoScalingAlgImpl(autoScalingClient, sns).continueAutoScaling(arbLifecycleHookNotification)

        (hook, group, result, instance) <- deferredCompleteLifecycleActionRequest.get
      } yield {
        assertEquals(LifecycleHookName(hook), arbLifecycleHookNotification.lifecycleHookName)
        assertEquals(AutoScalingGroupName(group), arbLifecycleHookNotification.autoScalingGroupName)
        assertEquals(result, LifecycleActionResult("CONTINUE"))
        assertEquals(instance.map(i => InstanceId(i.value)), arbLifecycleHookNotification.EC2InstanceId.some)
      }
    }
  }

  test("AutoScalingAlgImpl should pause 5 seconds and then send a message to restart the lambda, but only if the Lifecycle Action is still active") {
    forAllF { (arbSnsTopicArn: TopicARN,
               notifAndResp: (LifecycleHookNotification, AutoScalingInstancesType),
               guardState: LifecycleState,
              ) =>
      val (arbLifecycleHookNotification, arbAutoScalingInstancesType) = notifAndResp
      for {
        capturedPublishRequests <- Ref[IO].of(Set.empty[(TopicARN, Message)])
        control <- TestControl.execute {
          val autoScalingClient = new AutoScaling.Default[IO](new NotImplementedError().raiseError) {
            override def describeAutoScalingInstances(instanceIds: Option[List[XmlStringMaxLen19]],
                                                      maxRecords: Option[MaxRecords],
                                                      nextToken: Option[XmlString]): IO[AutoScalingInstancesType] =
              IO.pure {
                if (instanceIds.exists(_.contains(arbLifecycleHookNotification.EC2InstanceId.value)))
                  arbAutoScalingInstancesType
                else
                  AutoScalingInstancesType()
              }

            override def completeLifecycleAction(lifecycleHookName: AsciiStringMaxLen255,
                                                 autoScalingGroupName: ResourceName,
                                                 lifecycleActionResult: LifecycleActionResult,
                                                 lifecycleActionToken: Option[LifecycleActionToken],
                                                 instanceId: Option[XmlStringMaxLen19]): IO[CompleteLifecycleActionAnswer] =
              IO.raiseError(new RuntimeException("AutoScalingAsyncClient.completeLifecycleAction should not have been called"))
          }

          val sns = new SnsAlg[IO] {
            override def publish(topic: TopicARN, message: Message): IO[Unit] =
              capturedPublishRequests.update(_ + (topic -> message)).void
          }

          AutoScalingAlg[IO](autoScalingClient, sns)
            .pauseAndRecurse(arbSnsTopicArn, arbLifecycleHookNotification, guardState)
        }
        _ <- control.tick
        _ <- control.tickFor(4.seconds)

        firstShouldBeEmpty <- capturedPublishRequests.get

        _ <- control.tickAll
        _ <- control.tickAll

        _ <- control.advanceAndTick(1.minute)

        finalCapturedPublishRequests <- capturedPublishRequests.get
        result <- control.results
      } yield {
        assert(firstShouldBeEmpty.isEmpty)

        val arbLifecycleState: Option[LifecycleState] =
          arbAutoScalingInstancesType
            .autoScalingInstances
            .flatMap {
              _.collectFirstSome {
                case instance if InstanceId(instance.instanceId.value) == arbLifecycleHookNotification.EC2InstanceId =>
                  LifecycleState.fromString(instance.lifecycleState.value)
                case _ => None
              }
            }

        if (arbLifecycleState.contains(guardState)) {
          assert(finalCapturedPublishRequests.contains(arbSnsTopicArn -> Message(arbLifecycleHookNotification.asJson.noSpaces)))
        } else {
          assert(finalCapturedPublishRequests.isEmpty, s"Input Lifecycle State was $arbLifecycleState, not $guardState, so we should have stopped without publishing any messages")
        }
        assert(result.isDefined)
      }
    }
  }
}
