package com.dwolla.aws.sns.model

import com.amazonaws.regions.Regions
import com.dwolla.aws.AccountId
import com.dwolla.aws.ArbitraryInstances._
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary

object ArbitraryInstances extends ArbitraryInstances

trait ArbitraryInstances {
  implicit val arbSnsTopicArn: Arbitrary[SnsTopicArn] =
    Arbitrary(for {
      region <- arbitrary[Regions]
      accountId <- arbitrary[AccountId]
      topicName <- Gen.alphaNumStr
    } yield tagSnsTopicArn(s"arn:aws:sns:${region.name()}:$accountId:$topicName"))
}
