package com.dwolla.autoscaling.ecs.draining

import com.dwolla.aws.autoscaling.model.{AutoScalingSnsMessage, LifecycleHookNotification, TestNotification}
import com.dwolla.aws.sns.model.{SnsTopicArn, recordsLens}
import fs2.{Pipe, RaiseThrowable, Stream}
import io.chrisdavenport.log4cats.Logger
import io.circe.fs2.{byteStreamParser, decoder, stringStreamParser}

object ParseLifecycleHookNotification {

  def apply[F[_] : RaiseThrowable : Logger]: Pipe[F, Byte, (SnsTopicArn, LifecycleHookNotification)] = s =>
    for {
      json <- s.through(byteStreamParser)
      _ <- Stream.eval(Logger[F].info(s"Received message ${json.noSpaces}"))
      snsRecord <- Stream.emits(recordsLens(json))
      autoScalingMessage <- Stream.emit(snsRecord.sns.message).through(stringStreamParser).through(decoder[F, AutoScalingSnsMessage])
      notification <- autoScalingMessage match {
        case a: LifecycleHookNotification =>
          Stream.emit(a)
        case TestNotification(_, requestId, _, _, _, _, _) =>
          Stream.eval(Logger[F].info(s"ignoring TestNotification message $requestId")) >> Stream.empty
      }
    } yield snsRecord.sns.topicArn -> notification
}
