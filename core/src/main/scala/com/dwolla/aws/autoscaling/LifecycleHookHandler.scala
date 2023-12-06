package com.dwolla.aws.autoscaling

import cats.*
import cats.effect.{Trace as _, *}
import cats.mtl.Local
import cats.syntax.all.*
import com.amazonaws.sns.TopicARN
import com.dwolla.aws.sns.*
import feral.lambda.events.SnsEvent
import feral.lambda.{INothing, LambdaEnv}
import fs2.Stream
import natchez.{EntryPoint, Span, Trace}
import natchez.mtl.given
import org.typelevel.log4cats.{Logger, LoggerFactory}
import com.dwolla.tracing.syntax.*

object LifecycleHookHandler {
  def apply[F[_] : MonadCancelThrow : LoggerFactory : Logger](entryPoint: EntryPoint[F], hookName: String)
                                                             (eventBridge: (TopicARN, LifecycleHookNotification) => F[Unit])
                                                             (using fs2.Compiler[F, F], Local[F, Span[F]]): LambdaEnv[F, SnsEvent] => F[Option[INothing]] = env =>
    entryPoint.runInRoot(hookName) {
      Trace[F].kernel.flatMap(k => Logger[F].info(s"trace kernel: ${k.toHeaders}")) >>
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
      .flatTap { _ =>
        Logger[F].info("tracing should be complete by now")
      }
}
