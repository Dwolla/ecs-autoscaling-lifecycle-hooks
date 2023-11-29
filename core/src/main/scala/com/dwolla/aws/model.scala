package com.dwolla.aws

import alleycats.Empty
import com.github.plokhotnyuk.jsoniter_scala.core.WriterConfig
import monix.newtypes.NewtypeWrapped
import io.circe.{Decoder, Encoder}
import io.circe.syntax.*
import monix.newtypes.integrations.DerivedCirceCodec
import natchez.TraceableValue
import smithy4s.json.Json.payloadCodecs
import smithy4s.{Bijection, Newtype, Schema}

import scala.annotation.targetName

type AccountId = AccountId.Type
object AccountId extends NewtypeWrapped[String] with DerivedCirceCodec

type TagName = TagName.Type
object TagName extends NewtypeWrapped[String] with DerivedCirceCodec
type TagValue = TagValue.Type
object TagValue extends NewtypeWrapped[String] with DerivedCirceCodec

case class Tag(name: TagName,
               value: TagValue)
object Tag {
  given Encoder[Tag] = Encoder.forProduct2("name", "value") { t => (t.name, t.value) }
  given TraceableValue[List[Tag]] = TraceableValue.stringToTraceValue.contramap(_.asJson.noSpaces)
}

given[B <: Newtype[A]#Type, A: Encoder](using Bijection[A, B]): Encoder[B] = Encoder[A].contramap(summon[Bijection[A, B]].from)
given[B <: Newtype[A]#Type, A: Decoder](using Bijection[A, B]): Decoder[B] = Decoder[A].map(summon[Bijection[A, B]].to)

given[B <: Newtype[A]#Type, A: Empty](using Bijection[A, B]): Empty[B] = new Empty[B] {
  override def empty: B = summon[Bijection[A, B]].to(Empty[A].empty)
}

object TraceableValueInstances extends LowPriorityTraceableValueInstances {
  given[B <: Newtype[A]#Type, A: TraceableValue](using Bijection[A, B]): TraceableValue[B] = TraceableValue[A].contramap(summon[Bijection[A, B]].from)
  given[B <: monix.newtypes.Newtype[A]#Type, A: TraceableValue](using monix.newtypes.HasExtractor.Aux[B, A]): TraceableValue[B] = TraceableValue[A].contramap(summon[monix.newtypes.HasExtractor.Aux[B, A]].extract)
  given[A: Empty : TraceableValue]: TraceableValue[Option[A]] =
    TraceableValue[A].contramap(_.getOrElse(Empty[A].empty))
  @targetName("given_TraceableValue_A_by_schema")
  given[A](using schemaA: Schema[A]): TraceableValue[A] =
    TraceableValue.stringToTraceValue.contramap(asJsonString(schemaA))
  @targetName("given_TraceableValue_List_A_by_schema")
  given[A](using schemaA: Schema[A]): TraceableValue[List[A]] =
    TraceableValue.stringToTraceValue.contramap {
      asJsonString(Schema.list(schemaA))
    }
  @targetName("given_TraceableValue_Option_List_A_by_schema")
  given[A](using schemaA: Schema[A]): TraceableValue[Option[List[A]]] =
    TraceableValue.stringToTraceValue.contramap {
      asJsonString(Schema.list(schemaA).option)
    }

  @targetName("given_TraceableValue_List_A_as_json")
  given[A: Encoder]: TraceableValue[List[A]] =
    TraceableValue.stringToTraceValue.contramap(_.asJson.noSpaces)
}

trait LowPriorityTraceableValueInstances {
  @targetName("given_TraceableValue_Option_A_by_schema")
  given[A](using schemaA: Schema[A]): TraceableValue[Option[A]] =
    TraceableValue.stringToTraceValue.contramap {
      asJsonString(schemaA.option)
    }
}


private def asJsonString[A](schema: Schema[A])
                           (a: A): String =
  payloadCodecs
    .withJsoniterWriterConfig(WriterConfig.withIndentionStep(0))
    .encoders
    .fromSchema(schema)
    .encode(a)
    .toUTF8String
