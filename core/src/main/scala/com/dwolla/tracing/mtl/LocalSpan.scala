package com.dwolla.tracing.mtl

import cats.*
import cats.effect.{Trace as _, *}
import cats.mtl.Local
import natchez.Span

/**
 * hopefully https://github.com/typelevel/cats-effect/pull/3429 lands and 
 * this all goes away when we update to cats-effect 3.6.0
 */
object LocalSpan {
  /**
   * Given an `IOLocal[E]`, provides a `Local[IO, E]`.
   *
   * Copied from [[https://github.com/armanbilge/oxidized/blob/412be9cd0a60b901fd5f9157ea48bda8632c5527/core/src/main/scala/oxidized/instances/io.scala#L34-L43 armanbilge/oxidized]]
   * but hopefully this instance can be brought into cats-effect someday and
   * removed here. See [[https://github.com/typelevel/cats-effect/issues/3385 typelevel/cats-effect#3385]]
   * for more discussion.
   *
   * TODO remove if made more widely available upstream
   *
   * @param ioLocal the `IOLocal` that propagates the state of the `E` element
   * @tparam E the type of state to propagate
   * @return a `Local[IO, E]` backed by the given `IOLocal[E]`
   */
  def toLocal[E](ioLocal: IOLocal[E]): Local[IO, E] =
    new Local[IO, E] {
      override def local[A](fa: IO[A])(f: E => E): IO[A] =
        ioLocal.get.flatMap { initial =>
          ioLocal.set(f(initial)) >> fa.guarantee(ioLocal.set(initial))
        }

      override def applicative: Applicative[IO] = IO.asyncForIO

      override def ask[E2 >: E]: IO[E2] = ioLocal.get
    }

  def apply(initial: Span[IO] = Span.noop[IO]): IO[Local[IO, Span[IO]]] =
    IOLocal(initial).map(toLocal)
}
