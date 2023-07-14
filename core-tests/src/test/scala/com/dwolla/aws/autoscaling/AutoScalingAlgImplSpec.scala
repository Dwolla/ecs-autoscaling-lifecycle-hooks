package com.dwolla.aws.autoscaling

import cats.effect.*
import cats.effect.std.Dispatcher
import cats.effect.testkit.TestControl
import com.dwolla.aws.ArbitraryInstances
import com.dwolla.aws.autoscaling.LifecycleState.*
import com.dwolla.aws.sns.{SnsAlg, SnsTopicArn}
import com.dwolla.aws.ec2.Ec2InstanceId
import io.circe.syntax.*
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.effect.PropF.forAllF
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.noop.NoOpFactory
import software.amazon.awssdk.services.autoscaling.AutoScalingAsyncClient
import software.amazon.awssdk.services.autoscaling.model.{LifecycleState as _, *}

import java.util.concurrent.CompletableFuture
import scala.concurrent.duration.*
import scala.jdk.CollectionConverters.*

class AutoScalingAlgImplSpec
  extends CatsEffectSuite
  with ScalaCheckEffectSuite
  with ArbitraryInstances {

  private implicit val loggerFactory: LoggerFactory[IO] = NoOpFactory[IO]

  test("AutoScalingAlgImpl should make an async completeLifecycleAction request to continueAutoScaling") {
    forAllF { (arbLifecycleHookNotification: LifecycleHookNotification) =>
      Dispatcher.sequential[IO].use { dispatcher =>
        for {
          deferredCompleteLifecycleActionRequest <- Deferred[IO, CompleteLifecycleActionRequest]
          autoScalingClient = new AutoScalingAsyncClient {
            override def serviceName(): String = "FakeAutoScalingAsyncClient"
            override def close(): Unit = ()

            override def completeLifecycleAction(completeLifecycleActionRequest: CompleteLifecycleActionRequest): CompletableFuture[CompleteLifecycleActionResponse] =
              dispatcher.unsafeToCompletableFuture {
                deferredCompleteLifecycleActionRequest.complete(completeLifecycleActionRequest)
                  .as(CompleteLifecycleActionResponse.builder().build())
              }
          }

          sns = new SnsAlg[IO] {
            override def publish(topic: SnsTopicArn, message: String): IO[Unit] =
              IO.raiseError(new RuntimeException("SnsAsyncClient.publish should not have been called"))
          }

          _ <- new AutoScalingAlgImpl(autoScalingClient, sns).continueAutoScaling(arbLifecycleHookNotification)

          passedReq <- deferredCompleteLifecycleActionRequest.get
        } yield {
          assertEquals(LifecycleHookName(passedReq.lifecycleHookName()), arbLifecycleHookNotification.lifecycleHookName)
          assertEquals(AutoScalingGroupName(passedReq.autoScalingGroupName()), arbLifecycleHookNotification.autoScalingGroupName)
          assertEquals(passedReq.lifecycleActionResult(), "CONTINUE")
          assertEquals(passedReq.instanceId(), arbLifecycleHookNotification.EC2InstanceId.value)
        }
      }
    }
  }

  test("AutoScalingAlgImpl should pause 5 seconds and then send a message to restart the lambda, but only if the Lifecycle Action is still active") {
    forAllF { (arbSnsTopicArn: SnsTopicArn,
               notifAndResp: (LifecycleHookNotification, DescribeAutoScalingInstancesResponse),
               guardState: LifecycleState,
              ) =>
      val (arbLifecycleHookNotification, arbDescribeAutoScalingInstancesResponse) = notifAndResp
      for {
        capturedPublishRequests <- Ref[IO].of(Set.empty[(SnsTopicArn, String)])
        control <- TestControl.execute {
          Dispatcher.sequential[IO](await = true).use { dispatcher =>
            val autoScalingClient = new AutoScalingAsyncClient {
              override def serviceName(): String = "FakeAutoScalingAsyncClient"
              override def close(): Unit = ()

              override def describeAutoScalingInstances(req: DescribeAutoScalingInstancesRequest): CompletableFuture[DescribeAutoScalingInstancesResponse] =
                dispatcher.unsafeToCompletableFuture(IO {
                  if (req.instanceIds().contains(arbLifecycleHookNotification.EC2InstanceId.value))
                    arbDescribeAutoScalingInstancesResponse
                  else
                    DescribeAutoScalingInstancesResponse.builder().build()
                })

              override def completeLifecycleAction(completeLifecycleActionRequest: CompleteLifecycleActionRequest): CompletableFuture[CompleteLifecycleActionResponse] = {
                CompletableFuture.failedFuture(new RuntimeException("AutoScalingAsyncClient.completeLifecycleAction should not have been called"))
              }
            }

            val sns = new SnsAlg[IO] {
              override def publish(topic: SnsTopicArn, message: String): IO[Unit] =
                capturedPublishRequests.update(_ + (topic -> message)).void
            }

            AutoScalingAlg[IO](autoScalingClient, sns)
              .pauseAndRecurse(arbSnsTopicArn, arbLifecycleHookNotification, guardState)
          }
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
          arbDescribeAutoScalingInstancesResponse
            .autoScalingInstances()
            .asScala
            .collectFirst {
              case instance if Ec2InstanceId(instance.instanceId()) == arbLifecycleHookNotification.EC2InstanceId =>
                LifecycleState.fromString(instance.lifecycleState())
            }
            .flatten
        
        if (arbLifecycleState.contains(guardState)) {
          assert(finalCapturedPublishRequests.contains(arbSnsTopicArn -> arbLifecycleHookNotification.asJson.noSpaces))
        } else {
          assert(finalCapturedPublishRequests.isEmpty, s"Input Lifecycle State was $arbLifecycleState, not $guardState, so we should have stopped without publishing any messages")
        }
        assert(result.isDefined)
      }
    }
  }
}
