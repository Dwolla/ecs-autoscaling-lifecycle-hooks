package com.dwolla.autoscaling.ecs.draining

import com.dwolla.aws.autoscaling.model.LifecycleHookNotification
import com.dwolla.aws.sns.model.{SnsTopicArn, recordsLens}
import fs2.{Pipe, RaiseThrowable, Stream}
import io.circe.fs2.{byteStreamParser, decoder, stringStreamParser}
import io.chrisdavenport.log4cats.Logger

object ParseLifecycleHookNotification {
  def apply[F[_] : RaiseThrowable : Logger]: Pipe[F, Byte, (SnsTopicArn, LifecycleHookNotification)] = s =>
    for {
      json <- s.through(byteStreamParser)
      _ <- Stream.eval(Logger[F].info(s"Received message ${json.noSpaces}"))
      snsRecord <- Stream.emits(recordsLens(json))
      notification <- Stream.emit(snsRecord.sns.message).through(stringStreamParser).through(decoder[F, LifecycleHookNotification])
    } yield snsRecord.sns.topicArn -> notification
}
