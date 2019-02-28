package com.dwolla.aws.sns

import java.time.Instant

import cats.implicits._
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.optics.JsonPath._
import shapeless.tag.@@

package object model {
  private[model] implicit val config: Configuration = Configuration(
    transformMemberNames = _.capitalize,
    transformConstructorNames = _.capitalize,
    useDefaults = true,
    discriminator = None,
  )

  val recordsLens: Json => List[SnsRecord] = root.Records.each.as[SnsRecord].getAll

  type SnsTopicArn = String @@ SnsTopicArnTag

  val tagSnsTopicArn: String => SnsTopicArn = shapeless.tag[SnsTopicArnTag][String]

  implicit val snsTopicArnEncoder: Encoder[SnsTopicArn] = Encoder[String].narrow
  implicit val snsTopicArnDecoder: Decoder[SnsTopicArn] = Decoder[String].map(tagSnsTopicArn)
}

package model {
  case class SnsRecord(eventSource: String,
                       eventVersion: String,
                       eventSubscriptionArn: String,
                       sns: SnsMessage,
                      )

  object SnsRecord {
    implicit val snsRecordEncoder: Encoder[SnsRecord] = deriveEncoder[SnsRecord]
    implicit val snsRecordDecoder: Decoder[SnsRecord] = deriveDecoder[SnsRecord]
  }

  case class SnsMessage(`type`: String,
                        messageId: String,
                        topicArn: SnsTopicArn,
                        subject: Option[String],
                        message: String,
                        timestamp: Instant,
                       )

  object SnsMessage {
    implicit val snsMessageEncoder: Encoder[SnsMessage] = deriveEncoder[SnsMessage]
    implicit val snsMessageDecoder: Decoder[SnsMessage] = deriveDecoder[SnsMessage]
  }

  trait SnsTopicArnTag
}
