package com.dwolla.aws.autoscaling

import cats.*
import cats.syntax.all.*
import com.dwolla.aws.sns.*
import feral.lambda.{INothing, LambdaEnv}
import feral.lambda.events.SnsEvent
import fs2.Stream
import org.typelevel.log4cats.LoggerFactory

object LifecycleHookHandler {
  def apply[F[_] : MonadThrow : LoggerFactory](eventBridge: (SnsTopicArn, LifecycleHookNotification) => F[Unit])
                                              (using fs2.Compiler[F, F]): LambdaEnv[F, SnsEvent] => F[Option[INothing]] = env =>
    Stream.eval(env.event)
      .map(_.records)
      .flatMap(Stream.emits(_))
      .map(_.sns)
      .evalMap(ParseLifecycleHookNotification[F])
      .unNone
      .evalMap(eventBridge.tupled)
      .compile
      .drain
      .as(None)
}
