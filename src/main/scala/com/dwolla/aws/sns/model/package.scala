package com.dwolla.aws.sns

import java.time.Instant

import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.optics.JsonPath._

package object model {
  private[model] implicit val config: Configuration = Configuration(
    transformMemberNames = _.capitalize,
    transformConstructorNames = _.capitalize,
    useDefaults = true,
    discriminator = None,
  )

  val recordsLens: Json => List[SnsRecord] = root.Records.each.as[SnsRecord].getAll
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
                        topicArn: String,
                        subject: String,
                        message: String,
                        timestamp: Instant,
                       )

  object SnsMessage {
    implicit val snsMessageEncoder: Encoder[SnsMessage] = deriveEncoder[SnsMessage]
    implicit val snsMessageDecoder: Decoder[SnsMessage] = deriveDecoder[SnsMessage]
  }
}
