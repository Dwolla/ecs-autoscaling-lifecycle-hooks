package com.dwolla.aws.sns

import io.circe.*
import monix.newtypes.*

type SnsTopicArn = SnsTopicArn.Type
object SnsTopicArn extends NewtypeWrapped[String] {
  implicit val snsTopicArnEncoder: Encoder[SnsTopicArn] = Encoder[String].contramap(_.value)
  implicit val snsTopicArnDecoder: Decoder[SnsTopicArn] = Decoder[String].map(SnsTopicArn(_))
}
