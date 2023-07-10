package com.dwolla.aws.autoscaling

import _root_.io.circe.*
import _root_.io.circe.literal.*
import _root_.io.circe.syntax.*
import cats.effect.*
import cats.syntax.all.*
import com.dwolla.aws.ArbitraryInstances
import com.dwolla.aws.sns.SnsTopicArn
import feral.lambda.events.SnsEvent
import feral.lambda.{Context, ContextInstances, LambdaEnv}
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.effect.PropF.forAllF
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.noop.NoOpFactory

class LifecycleHookHandlerSpec
  extends CatsEffectSuite
    with ScalaCheckEffectSuite
    with ArbitraryInstances
    with ContextInstances {

  private implicit val noopLoggerFactory: LoggerFactory[IO] = NoOpFactory[IO]
  private def snsMessage[T: Encoder](topic: SnsTopicArn, detail: T, maybeSubject: Option[String]): Json =
    json"""{
             "Records": [
               {
                 "EventSource": "aws:sns",
                 "EventVersion": "1.0",
                 "EventSubscriptionArn": "arn:aws:sns:us-west-2:{{accountId}}:ExampleTopic",
                 "Sns": {
                   "Type": "Notification",
                   "MessageId": "95df01b4-ee98-5cb9-9903-4c221d41eb5e",
                   "TopicArn": ${topic.asJson},
                   "Subject": ${maybeSubject.asJson},
                   "Message": ${detail.asJson.noSpaces},
                   "Timestamp": "1970-01-01T00:00:00.000Z",
                   "SignatureVersion": "1",
                   "Signature": "EXAMPLE",
                   "SigningCertUrl": "EXAMPLE",
                   "UnsubscribeUrl": "EXAMPLE",
                   "MessageAttributes": {
                     "Test": {
                       "Type": "String",
                       "Value": "TestString"
                     },
                     "TestBinary": {
                       "Type": "Binary",
                       "Value": "TestBinary"
                     }
                   }
                 }
               }
             ]
           }"""

  private val testNotification =
    json"""{
             "AccountId": "006467937747",
             "RequestId": "4781ed87-354a-11e9-9f76-cb62a7958e88",
             "AutoScalingGroupARN": "arn:aws:autoscaling:us-west-2:006467937747:autoScalingGroup:3c1d7d91-2140-40b1-a8b1-c9f46e4dff13:autoScalingGroupName/ecs-default-cluster-Sandbox-EcsClusterAutoScaleGroupSandbox-CLJKMQ0V93DL",
             "AutoScalingGroupName": "ecs-default-cluster-Sandbox-EcsClusterAutoScaleGroupSandbox-CLJKMQ0V93DL",
             "Service": "AWS Auto Scaling",
             "Event": "autoscaling:TEST_NOTIFICATION",
             "Time": "2019-02-20T20:01:15.747Z"
           }"""

  test("LifecycleHookHandler should handle a message") {
    forAllF { (arbSnsTopicArn: SnsTopicArn,
               arbContext: Context[IO],
               arbLifecycleHookNotification: LifecycleHookNotification,
               arbSubject: Option[String],
              ) =>
      for {
        deferredLifecycleHookNotification <- Deferred[IO, LifecycleHookNotification]
        deferredSnsTopicArn <- Deferred[IO, SnsTopicArn]

        eventHandler = LifecycleHookHandler { case (arn, notif) =>
          deferredLifecycleHookNotification.complete(notif) >> 
            deferredSnsTopicArn.complete(arn).void
        }

        snsEvent <- snsMessage(arbSnsTopicArn, arbLifecycleHookNotification, arbSubject).as[SnsEvent].liftTo[IO]
        output <- eventHandler(LambdaEnv.pure(snsEvent, arbContext))

        actualLifecycleHookNotification <- deferredLifecycleHookNotification.get
        actualSnsTopicArn <- deferredSnsTopicArn.get
      } yield {
        assertEquals(actualSnsTopicArn, arbSnsTopicArn)
        assertEquals(actualLifecycleHookNotification, arbLifecycleHookNotification)
        assertEquals(output, None)
      }
    }
  }

  test("LifecycleHookHandler should handle a test notification message") {
    forAllF { (arbSnsTopicArn: SnsTopicArn,
               arbContext: Context[IO],
               arbSubject: Option[String],
              ) =>
      val eventHandler = LifecycleHookHandler { case (arn, notif) =>
        IO(fail(s"TerminationEventHandler should not be called for test messages, but was called with $arn and $notif"))
      }

      for {
        snsEvent <- snsMessage(arbSnsTopicArn, testNotification, arbSubject).as[SnsEvent].liftTo[IO]
        output <- eventHandler(LambdaEnv.pure(snsEvent, arbContext))
      } yield {
        assertEquals(output, None)
      }
    }
  }
}
