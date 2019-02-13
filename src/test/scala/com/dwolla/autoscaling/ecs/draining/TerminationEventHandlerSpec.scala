package com.dwolla.autoscaling.ecs.draining

import java.io.ByteArrayOutputStream

import _root_.fs2.{Stream, Scope => _}
import _root_.io.circe._
import _root_.io.circe.literal._
import _root_.io.circe.syntax._
import cats.effect._
import cats.effect.concurrent.Deferred
import com.amazonaws.services.autoscaling.AmazonAutoScalingAsync
import com.amazonaws.services.ecs.AmazonECSAsync
import com.amazonaws.services.ecs.model.{Cluster => _, ContainerInstance => _, Resource => _}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.AmazonSNSAsync
import com.amazonaws.util.StringInputStream
import com.dwolla.aws.ArbitraryInstances._
import com.dwolla.aws.autoscaling.model.LifecycleHookNotification
import com.dwolla.aws.autoscaling.{AutoScalingAlg, AutoScalingAlgImpl}
import com.dwolla.aws.ecs.{EcsAlg, EcsAlgImpl}
import com.dwolla.aws.lambda.fs2.LambdaContext._
import com.dwolla.aws.sns.model.SnsTopicArn
import org.specs2.ScalaCheck
import org.specs2.matcher.{IOImplicits, IOMatchers, Matchers}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class TerminationEventHandlerSpec extends Specification with IOMatchers with IOImplicits with Matchers with ScalaCheck with Mockito {
  private def snsMessage[T: Encoder](topic: SnsTopicArn, detail: T, maybeSubject: Option[String]): Json =
    snsMessage(topic.asJson, detail.asJson, maybeSubject.asJson)

  private def snsMessage(topic: Json, detail: Json, subject: Json): Json =
    json"""{
             "Records": [
               {
                 "EventSource": "aws:sns",
                 "EventVersion": "1.0",
                 "EventSubscriptionArn": "arn:aws:sns:us-west-2:{{accountId}}:ExampleTopic",
                 "Sns": {
                   "Type": "Notification",
                   "MessageId": "95df01b4-ee98-5cb9-9903-4c221d41eb5e",
                   "TopicArn": $topic,
                   "Subject": $subject,
                   "Message": ${detail.noSpaces},
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

  "TerminationEventHandler" should {
    "handle a message" >> { prop { (arbSnsTopicArn: SnsTopicArn,
                                    arbitraryContext: Context,
                                    arbitraryLifecycleHookNotification: LifecycleHookNotification,
                                    arbSubject: Option[String],
                                   ) =>
      val baos = new ByteArrayOutputStream()
      val ecsClient: AmazonECSAsync = mock[AmazonECSAsync]
      val autoScalingClient: AmazonAutoScalingAsync = mock[AmazonAutoScalingAsync]
      val snsClient = mock[AmazonSNSAsync]

      for {
        deferredEcsInterpreter <- Deferred[IO, EcsAlg[IO, Stream[IO, ?]]]
        deferredAutoScalingInterpreter <- Deferred[IO, AutoScalingAlg[IO]]
        deferredLifecycleHookNotification <- Deferred[IO, LifecycleHookNotification]
        deferredSnsTopicArn <- Deferred[IO, SnsTopicArn]

        eventHandler = new TerminationEventHandler(
          Resource.pure(ecsClient),
          Resource.pure(autoScalingClient),
          Resource.pure(snsClient),
          (ecs, asg) => (a, l) => for {
            _ <- deferredSnsTopicArn.complete(a)
            _ <- deferredLifecycleHookNotification.complete(l)
            _ <- deferredEcsInterpreter.complete(ecs)
            _ <- deferredAutoScalingInterpreter.complete(asg)
          } yield ()
        )

        _ <- IO(eventHandler.handleRequest(new StringInputStream(snsMessage(arbSnsTopicArn, arbitraryLifecycleHookNotification, arbSubject).noSpaces), baos, arbitraryContext))

        actualLifecycleHookNotification <- deferredLifecycleHookNotification.get
        actualEcsInterpreter <- deferredEcsInterpreter.get
        actualAutoScalingInterpreter <- deferredAutoScalingInterpreter.get
        actualSnsTopicArn <- deferredSnsTopicArn.get
      } yield {
        actualSnsTopicArn must be_==(arbSnsTopicArn)
        actualLifecycleHookNotification must be_==(arbitraryLifecycleHookNotification)
        baos.toByteArray must beEmpty

        actualEcsInterpreter must beAnInstanceOf[EcsAlgImpl[IO]]
        actualAutoScalingInterpreter must beAnInstanceOf[AutoScalingAlgImpl[IO]]
      }
    }}

  }

}
