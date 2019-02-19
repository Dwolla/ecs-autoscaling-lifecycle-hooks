package com.dwolla

import cats.Applicative
import io.chrisdavenport.log4cats.Logger

class NoOpLogger[F[_] : Applicative] extends Logger[F] {
  override def error(t: Throwable)(message: => String): F[Unit] = Applicative[F].unit
  override def warn(t: Throwable)(message: => String): F[Unit] = Applicative[F].unit
  override def info(t: Throwable)(message: => String): F[Unit] = Applicative[F].unit
  override def debug(t: Throwable)(message: => String): F[Unit] = Applicative[F].unit
  override def trace(t: Throwable)(message: => String): F[Unit] = Applicative[F].unit
  override def error(message: => String): F[Unit] = Applicative[F].unit
  override def warn(message: => String): F[Unit] = Applicative[F].unit
  override def info(message: => String): F[Unit] = Applicative[F].unit
  override def debug(message: => String): F[Unit] = Applicative[F].unit
  override def trace(message: => String): F[Unit] = Applicative[F].unit
}

object NoOpLogger {
  def apply[F[_] : Applicative]: Logger[F] = new NoOpLogger[F]
}
