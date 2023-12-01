package com.dwolla.aws.sns

import cats.*
import cats.syntax.all.*
import org.typelevel.log4cats.*
import com.amazonaws.sns.*

trait SnsAlg[F[_]] {
  def publish(topic: TopicARN, message: Message): F[Unit]
}

object SnsAlg {
  def apply[F[_] : Monad : LoggerFactory](client: SNS[F]): SnsAlg[F] = new SnsAlg[F] {
    override def publish(topic: TopicARN, message: Message): F[Unit] =
      LoggerFactory[F].create.flatMap { case given Logger[F] =>
        Logger[F].trace(s"Publishing message to $topic") *>
          client.publish(message, topic.some).void
      }
  }
}
