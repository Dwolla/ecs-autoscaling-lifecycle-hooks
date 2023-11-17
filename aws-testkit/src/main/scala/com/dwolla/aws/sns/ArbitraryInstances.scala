package com.dwolla.aws.sns

import com.amazonaws.sns.*
import com.dwolla.aws.ecs.given
import com.dwolla.aws.{AccountId, given}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import smithy4s.aws.AwsRegion

given Arbitrary[TopicARN] =
  Arbitrary(for {
    region <- arbitrary[AwsRegion]
    accountId <- arbitrary[AccountId]
    topicName <- Gen.alphaNumStr
  } yield TopicARN(s"arn:aws:sns:$region:$accountId:$topicName"))

given Arbitrary[Message] =
  Arbitrary(arbitrary[String].map(Message.apply))

given Arbitrary[MessageId] =
  Arbitrary(Gen.identifier.map(MessageId.apply))
