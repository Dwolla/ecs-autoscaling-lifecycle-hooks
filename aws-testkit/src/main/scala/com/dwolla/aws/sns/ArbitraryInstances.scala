package com.dwolla.aws.sns

import com.dwolla.aws.{AccountId, given}
import com.dwolla.aws.ecs.{Region, given}
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary

given Arbitrary[SnsTopicArn] =
  Arbitrary(for {
    region <- arbitrary[Region]
    accountId <- arbitrary[AccountId]
    topicName <- Gen.alphaNumStr
  } yield SnsTopicArn(s"arn:aws:sns:$region:$accountId:$topicName"))
