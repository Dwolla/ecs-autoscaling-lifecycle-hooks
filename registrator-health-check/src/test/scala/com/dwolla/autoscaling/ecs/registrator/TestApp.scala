package com.dwolla.autoscaling.ecs.registrator

import cats.*
import cats.effect.*
import cats.effect.std.Console
import cats.mtl.Local
import cats.syntax.all.*
import com.amazonaws.ec2.*
import com.dwolla.aws.ec2.{*, given}
import com.dwolla.tracing.mtl.LocalSpan
import com.dwolla.tracing.syntax.*
import com.dwolla.tracing.{DwollaEnvironment, OpenTelemetryAtDwolla}
import natchez.Span
import natchez.mtl.given
import org.http4s.*
import org.http4s.client.{Client, middleware}
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.*
import smithy4s.aws.*
import smithy4s.aws.kernel.AwsRegion

object TestApp extends ResourceApp.Simple {
  private val id = InstanceId("i-04d30e1ebb1109aa2")
  override def run: Resource[IO, Unit] =
    OpenTelemetryAtDwolla[IO]("ecs-autoscaling-scale-out-hook", DwollaEnvironment.Local)
      .flatMap(_.root("EC2 Test App"))
      .evalMap(LocalSpan(_))
      .flatMap { case given Local[IO, Span[IO]] =>
        given LoggerFactory[IO] = new ConsoleLogger[IO]

        EmberClientBuilder.default[IO].build
          .map(middleware.Logger(true, true, logAction = (IO.println(_: String)).some))
          .flatMap(AwsEnvironment.default(_, AwsRegion.US_WEST_2))
          .flatMap(AwsClient(EC2, _))
          .map(_.traceWithInputsAndOutputs)
          .map(Ec2Alg(_))
          .evalTap(_ => IO.println("using client via Ec2Alg"))
          .evalTap(_.getTagsForInstance(id) >>= IO.println)
          .void
      }

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
