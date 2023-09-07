package com.dwolla.aws

import org.scalacheck.*

given Arbitrary[AccountId] =
  Arbitrary(Gen.stringOfN(12, Gen.numChar).map(AccountId(_)))

val genTag: Gen[Tag] =
  for {
    key <- Gen.asciiPrintableStr.map(TagName(_))
    value <- Gen.asciiPrintableStr.map(TagValue(_))
  } yield Tag(key, value)
given Arbitrary[Tag] = Arbitrary(genTag)
