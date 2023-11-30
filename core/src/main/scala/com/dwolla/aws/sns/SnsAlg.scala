package com.dwolla.aws
package sns

import cats.*
import cats.syntax.all.*
import cats.tagless.Trivial
import cats.tagless.aop.Aspect
import com.amazonaws.sns.*
import com.dwolla.aws.TraceableValueInstances.given
import natchez.TraceableValue
import org.typelevel.log4cats.*

trait SnsAlg[F[_]] {
  def publish(topic: TopicARN, message: Message): F[Unit]
}

object SnsAlg {
  given Aspect[SnsAlg, TraceableValue, Trivial] = new Aspect[SnsAlg, TraceableValue, Trivial] {
    override def weave[F[_]](af: SnsAlg[F]): SnsAlg[[A] =>> Aspect.Weave[F, TraceableValue, Trivial, A]] =
      new SnsAlg[[A] =>> Aspect.Weave[F, TraceableValue, Trivial, A]] {
        override def publish(topic: TopicARN, message: Message): Aspect.Weave[F, TraceableValue, Trivial, Unit] =
          Aspect.Weave(
            "SnsAlg",
            List(List(
              Aspect.Advice.byValue[TraceableValue, TopicARN]("topic", topic),
              Aspect.Advice.byValue[TraceableValue, Message]("message", message),
            )),
            Aspect.Advice[F, Trivial, Unit]("publish", af.publish(topic, message))
          )
      }

    override def mapK[F[_], G[_]](af: SnsAlg[F])
                                 (fk: F ~> G): SnsAlg[G] =
      new SnsAlg[G] {
        override def publish(topic: TopicARN, message: Message): G[Unit] =
          fk(af.publish(topic, message))
      }
  }

  def apply[F[_] : Monad : LoggerFactory](client: SNS[F]): SnsAlg[F] = new SnsAlg[F] {
    override def publish(topic: TopicARN, message: Message): F[Unit] =
      LoggerFactory[F].create.flatMap { case given Logger[F] =>
        Logger[F].trace(s"Publishing message to $topic") *>
          client.publish(message, topic.some).void
      }
  }
}
