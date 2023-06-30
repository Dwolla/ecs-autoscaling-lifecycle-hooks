package com.dwolla.autoscaling.ecs.draining

import cats.*
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import com.amazonaws.ecs.ECS
import com.dwolla.aws.ecs.EcsAlg
import fs2.Stream
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.*
import smithy4s.aws.http4s.*
import smithy4s.aws.kernel.AwsRegion

object TestApp extends IOApp.Simple {
  override def run: IO[Unit] =
    Stream.resource {
      for {
        client <- EmberClientBuilder.default[IO].build
        given LoggerFactory[IO] = new ConsoleLogger[IO]
        ecs <- ECS.simpleAwsClient(client, AwsRegion.US_WEST_2).map(EcsAlg(_))
      } yield ecs
    }
      .flatMap { ecs =>
        ecs.listClusterArns
          .filter(_.value.contains("Production"))
          .flatMap(ecs.listContainerInstances)
      }
      .evalMap(c => IO.println(c))
      .compile
      .drain
}

class ConsoleLogger[F[_] : Applicative : Console] extends LoggerFactory[F] {
  override def getLoggerFromName(name: String): SelfAwareStructuredLogger[F] =
    new SelfAwareStructuredLogger[F] {
      override def isTraceEnabled: F[Boolean] = true.pure[F]
      override def isDebugEnabled: F[Boolean] = true.pure[F]
      override def isInfoEnabled: F[Boolean] = true.pure[F]
      override def isWarnEnabled: F[Boolean] = true.pure[F]
      override def isErrorEnabled: F[Boolean] = true.pure[F]
      override def trace(ctx: Map[String, String])(msg: => String): F[Unit] = Console[F].println(msg)
      override def trace(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] = Console[F].println(msg)
      override def debug(ctx: Map[String, String])(msg: => String): F[Unit] = Console[F].println(msg)
      override def debug(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] = Console[F].println(msg)
      override def info(ctx: Map[String, String])(msg: => String): F[Unit] = Console[F].println(msg)
      override def info(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] = Console[F].println(msg)
      override def warn(ctx: Map[String, String])(msg: => String): F[Unit] = Console[F].println(msg)
      override def warn(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] = Console[F].println(msg)
      override def error(ctx: Map[String, String])(msg: => String): F[Unit] = Console[F].println(msg)
      override def error(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] = Console[F].println(msg)
      override def error(t: Throwable)(message: => String): F[Unit] = Console[F].println(message)
      override def warn(t: Throwable)(message: => String): F[Unit] = Console[F].println(message)
      override def info(t: Throwable)(message: => String): F[Unit] = Console[F].println(message)
      override def debug(t: Throwable)(message: => String): F[Unit] = Console[F].println(message)
      override def trace(t: Throwable)(message: => String): F[Unit] = Console[F].println(message)
      override def error(message: => String): F[Unit] = Console[F].println(message)
      override def warn(message: => String): F[Unit] = Console[F].println(message)
      override def info(message: => String): F[Unit] = Console[F].println(message)
      override def debug(message: => String): F[Unit] = Console[F].println(message)
      override def trace(message: => String): F[Unit] = Console[F].println(message)
    }
  override def fromName(name: String): F[SelfAwareStructuredLogger[F]] =
    getLoggerFromName(name).pure[F]
}
