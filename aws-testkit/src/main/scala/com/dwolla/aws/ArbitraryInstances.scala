package com.dwolla.aws

import org.scalacheck.*
import smithy4s.Timestamp

given Arbitrary[AccountId] =
  Arbitrary(Gen.stringOfN(12, Gen.numChar).map(AccountId(_)))

val genTag: Gen[Tag] =
  for {
    key <- Gen.asciiPrintableStr.map(TagName(_))
    value <- Gen.asciiPrintableStr.map(TagValue(_))
  } yield Tag(key, value)
given Arbitrary[Tag] = Arbitrary(genTag)

/**
 * Timestamp doesn't allow the full range from Instant, so we can't just 
 * use an Arbitrary[Instant] and map it to Timestamp
 */
val genTimestamp: Gen[Timestamp] =
  for {
    epoch <- Gen.chooseNum(-62167219200L, 253402300799L)
    nanos <- Gen.chooseNum(0, 999999999)
  } yield Timestamp(epoch, nanos)
given Arbitrary[Timestamp] = Arbitrary(genTimestamp)
