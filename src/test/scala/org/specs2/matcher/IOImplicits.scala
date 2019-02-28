package org.specs2.matcher

import cats.Monoid
import cats.effect._
import cats.implicits._
import org.specs2.execute.{AsResult, Result}
import org.specs2.specification.core.{AsExecution, Execution}

trait IOImplicits {
  implicit def ioAsExecution[R: AsResult]: AsExecution[IO[R]] = new AsExecution[IO[R]] {
    def execute(r: => IO[R]): Execution = Execution.withEnvAsync(env => (IO.shift(env.executionContext) >> r).unsafeToFuture())
  }

  implicit def ioToResult[T: AsResult](io: IO[T]): Result = AsResult(io.unsafeRunSync())

  implicit val resultMonoid: Monoid[Result] = new Monoid[Result] {
    override def empty: Result = Result.ResultMonoid.zero

    override def combine(x: Result, y: Result): Result = Result.ResultMonoid.append(x, y)
  }
}
