package com.dwolla.aws.sns

import cats.effect.*
import cats.syntax.all.*
import software.amazon.awssdk.services.sns.SnsAsyncClient
import org.typelevel.log4cats.*
import software.amazon.awssdk.services.sns.model.PublishRequest

trait SnsAlg[F[_]] {
  def publish(topic: SnsTopicArn, message: String): F[Unit]
}

object SnsAlg {
  def apply[F[_] : Async : LoggerFactory](client: SnsAsyncClient): SnsAlg[F] = new SnsAlg[F] {
    override def publish(topic: SnsTopicArn, message: String): F[Unit] =
      LoggerFactory[F].create.flatMap { implicit l =>
        val req = PublishRequest.builder()
          .topicArn(topic.value)
          .message(message)
          .build()

        Logger[F].trace(s"Publishing message to $topic") >>
          Async[F]
            .fromCompletableFuture {
              Sync[F].delay(client.publish(req))
            }
            .void
      }

  }
}
