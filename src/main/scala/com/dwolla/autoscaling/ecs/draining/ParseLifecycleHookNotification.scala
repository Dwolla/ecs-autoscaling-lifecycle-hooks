package com.dwolla.autoscaling.ecs.draining

import com.dwolla.aws.autoscaling.model.LifecycleHookNotification
import com.dwolla.aws.sns.model.recordsLens
import fs2.{Pipe, RaiseThrowable, Stream}
import io.circe.fs2.{byteStreamParser, decoder, stringStreamParser}

object ParseLifecycleHookNotification {
  def apply[F[_] : RaiseThrowable]: Pipe[F, Byte, LifecycleHookNotification] =
    _.through(byteStreamParser)
      .flatMap(json => Stream.emits(recordsLens(json)))
      .flatMap(snsRecord => Stream.emit(snsRecord.sns.message))
      .through(stringStreamParser)
      .through(decoder[F, LifecycleHookNotification])
}
