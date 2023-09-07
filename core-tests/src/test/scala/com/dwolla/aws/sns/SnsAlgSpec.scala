package com.dwolla.aws.sns

import cats.effect.*
import cats.effect.std.Dispatcher
import com.dwolla.aws
import com.dwolla.aws.sns.given
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.effect.PropF.forAllF
import org.typelevel.log4cats.*
import org.typelevel.log4cats.noop.NoOpFactory
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.{PublishRequest, PublishResponse}

import java.util.UUID
import java.util.concurrent.CompletableFuture

class SnsAlgSpec
  extends CatsEffectSuite
    with ScalaCheckEffectSuite {

  given LoggerFactory[IO] = NoOpFactory[IO]

  test("SnsAlg should publish a message using the underlying client") {
    forAllF { (topic: SnsTopicArn, message: String, messageId: UUID) =>
      Dispatcher.sequential[IO].use { dispatcher =>
        for {
          deferredTopicAndMessage <- Deferred[IO, (SnsTopicArn, String)]
          fakeClient = new SnsAsyncClient {
            override def serviceName(): String = "FakeSnsAsyncClient"
            override def close(): Unit = ()

            override def publish(publishRequest: PublishRequest): CompletableFuture[PublishResponse] =
              dispatcher.unsafeToCompletableFuture {
                deferredTopicAndMessage.complete(SnsTopicArn(publishRequest.topicArn()) -> publishRequest.message())
                  .as(PublishResponse.builder().messageId(messageId.toString).build())
              }
          }

          _ <- SnsAlg[IO](fakeClient).publish(topic, message)

          (capturedTopic, capturedMessage) <- deferredTopicAndMessage.get
        } yield {
          assertEquals(capturedTopic, topic)
          assertEquals(capturedMessage, message)
        }
      }
    }
  }
}
