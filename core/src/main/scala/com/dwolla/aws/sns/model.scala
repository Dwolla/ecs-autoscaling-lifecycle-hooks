package com.dwolla.aws.sns

import io.circe.*
import monix.newtypes.*

type SnsTopicArn = SnsTopicArn.Type
object SnsTopicArn extends NewtypeWrapped[String] {
  given Encoder[SnsTopicArn] = Encoder[String].contramap(_.value)
  given Decoder[SnsTopicArn] = Decoder[String].map(SnsTopicArn(_))
}
