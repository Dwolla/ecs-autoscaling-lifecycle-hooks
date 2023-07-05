package com.dwolla.aws.autoscaling

import cats.effect.*
import cats.effect.std.Dispatcher
import cats.effect.testkit.TestControl
import com.dwolla.aws.ArbitraryInstances
import com.dwolla.aws.autoscaling.model.LifecycleHookNotification
import com.dwolla.aws.sns.model.SnsTopicArn
import io.circe.syntax.*
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.effect.PropF.forAllF
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.noop.NoOpFactory
import software.amazon.awssdk.services.autoscaling.AutoScalingAsyncClient
import software.amazon.awssdk.services.autoscaling.model.{CompleteLifecycleActionRequest, CompleteLifecycleActionResponse}
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.{PublishRequest, PublishResponse}

import java.util.concurrent.CompletableFuture
import scala.concurrent.duration.*

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

          snsClient = new SnsAsyncClient {
            override def serviceName(): String = "FakeSnsAsyncClient"
            override def close(): Unit = ()

            override def publish(publishRequest: PublishRequest): CompletableFuture[PublishResponse] =
              CompletableFuture.failedFuture(new RuntimeException("SnsAsyncClient.publish should not have been called"))
          }

          _ <- new AutoScalingAlgImpl[IO](autoScalingClient, snsClient).continueAutoScaling(arbLifecycleHookNotification)

          passedReq <- deferredCompleteLifecycleActionRequest.get
        } yield {
          assertEquals(passedReq.lifecycleHookName(), arbLifecycleHookNotification.lifecycleHookName)
          assertEquals(passedReq.autoScalingGroupName(), arbLifecycleHookNotification.autoScalingGroupName)
          assertEquals(passedReq.lifecycleActionResult(), "CONTINUE")
          assertEquals(passedReq.instanceId(), arbLifecycleHookNotification.EC2InstanceId.value)
        }
      }
    }
  }

  test("AutoScalingAlgImpl should pause 5 seconds and then send a message to restart the lambda") {
    forAllF { (arbSnsTopicArn: SnsTopicArn, arbLifecycleHookNotification: LifecycleHookNotification) =>
      for {
        capturedPublishRequest <- Deferred[IO, PublishRequest]
        control <- TestControl.execute {
          Dispatcher.sequential[IO](await = true).use { dispatcher =>
            val autoScalingClient = new AutoScalingAsyncClient {
              override def serviceName(): String = "FakeAutoScalingAsyncClient"
              override def close(): Unit = ()

              override def completeLifecycleAction(completeLifecycleActionRequest: CompleteLifecycleActionRequest): CompletableFuture[CompleteLifecycleActionResponse] = {
                CompletableFuture.failedFuture(new RuntimeException("AutoScalingAsyncClient.completeLifecycleAction should not have been called"))
              }
            }

            val snsClient = new SnsAsyncClient {
              override def serviceName(): String = "FakeSnsAsyncClient"
              override def close(): Unit = ()

              override def publish(publishRequest: PublishRequest): CompletableFuture[PublishResponse] =
                dispatcher.unsafeToCompletableFuture {
                  capturedPublishRequest
                    .complete(publishRequest)
                    .as(PublishResponse.builder().build())
                }
            }

            AutoScalingAlg[IO](autoScalingClient, snsClient)
              .pauseAndRecurse(arbSnsTopicArn, arbLifecycleHookNotification)
          }
        }
        _ <- control.tick
        _ <- control.tickFor(4.seconds)

        firstIsEmpty <- capturedPublishRequest.tryGet //.map(_.isEmpty)

        _ <- control.tickAll
        _ <- control.tickAll

        _ <- control.advanceAndTick(1.minute)

        publishedRequest <- capturedPublishRequest.get.timeout(2.seconds)
        result <- control.results
      } yield {
        assert(firstIsEmpty.isEmpty)
        assertEquals(publishedRequest.topicArn(), arbSnsTopicArn.value)
        assertEquals(publishedRequest.message(), arbLifecycleHookNotification.asJson.noSpaces)
        assert(result.isDefined)
      }
    }
  }
}
