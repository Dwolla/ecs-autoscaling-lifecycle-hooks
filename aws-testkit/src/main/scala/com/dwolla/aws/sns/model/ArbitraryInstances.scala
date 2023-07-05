package com.dwolla.aws.sns.model

import com.dwolla.aws.AccountId
import com.dwolla.aws.ArbitraryInstances.*
import com.dwolla.aws.ecs.model.Region
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary

object ArbitraryInstances extends ArbitraryInstances

trait ArbitraryInstances {
  implicit val arbSnsTopicArn: Arbitrary[SnsTopicArn] =
    Arbitrary(for {
      region <- arbitrary[Region]
      accountId <- arbitrary[AccountId]
      topicName <- Gen.alphaNumStr
    } yield SnsTopicArn(s"arn:aws:sns:$region:$accountId:$topicName"))
}
