package com.dwolla.aws.sns

import cats.effect.*
import cats.syntax.all.*
import com.amazonaws.sns.*
import com.dwolla.aws
import com.dwolla.aws.sns.given
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.effect.PropF.forAllF
import org.typelevel.log4cats.*
import org.typelevel.log4cats.noop.NoOpFactory

class SnsAlgSpec
  extends CatsEffectSuite
    with ScalaCheckEffectSuite {

  given LoggerFactory[IO] = NoOpFactory[IO]

  test("SnsAlg should publish a message using the underlying client") {
    forAllF { (topic: TopicARN, message: Message, messageId: MessageId) =>
      for {
        deferredTopicAndMessage <- Deferred[IO, (Option[TopicARN], Message)]
        fakeClient = new SNS.Default[IO](new NotImplementedError().raiseError) {
          override def publish(message: Message,
                               topicArn: Option[TopicARN],
                               targetArn: Option[String],
                               phoneNumber: Option[String],
                               subject: Option[Subject],
                               messageStructure: Option[MessageStructure],
                               messageAttributes: Option[Map[String, MessageAttributeValue]],
                               messageDeduplicationId: Option[String],
                               messageGroupId: Option[String]): IO[PublishResponse] =
            deferredTopicAndMessage.complete(topicArn -> message).as(PublishResponse())
        }

        _ <- SnsAlg[IO](fakeClient).publish(topic, message)

        (capturedTopic, capturedMessage) <- deferredTopicAndMessage.get
      } yield {
        assertEquals(capturedTopic, topic.some)
        assertEquals(capturedMessage, message)
      }
    }
  }
}
