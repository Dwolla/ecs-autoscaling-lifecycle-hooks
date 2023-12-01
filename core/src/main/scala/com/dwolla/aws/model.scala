package com.dwolla.aws

import monix.newtypes.NewtypeWrapped
import io.circe.{Decoder, Encoder}
import smithy4s.{Bijection, Newtype}

type AccountId = AccountId.Type
object AccountId extends NewtypeWrapped[String]

type TagName = TagName.Type
object TagName extends NewtypeWrapped[String]
type TagValue = TagValue.Type
object TagValue extends NewtypeWrapped[String]

case class Tag(name: TagName,
               value: TagValue)

given[B <: Newtype[A]#Type, A: Encoder](using Bijection[A, B]): Encoder[B] = Encoder[A].contramap(summon[Bijection[A, B]].from)
given[B <: Newtype[A]#Type, A: Decoder](using Bijection[A, B]): Decoder[B] = Decoder[A].map(summon[Bijection[A, B]].to)
