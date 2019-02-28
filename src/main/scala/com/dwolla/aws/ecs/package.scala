package com.dwolla.aws

import cats._
import cats.effect._
import com.amazonaws.services.ecs.{AmazonECSAsync, AmazonECSAsyncClientBuilder}
import fs2._

package object ecs {
  def resource[F[_] : Sync]: Resource[F, AmazonECSAsync] =
    Resource.make(Sync[F].delay(AmazonECSAsyncClientBuilder.defaultClient()))(c => Sync[F].delay(c.shutdown()))

  implicit def streamFF[F[_]] = new FunctorFilter[Stream[F, ?]] {
    override def functor = Functor[Stream[F, ?]]

    override def mapFilter[A, B](fa: Stream[F, A])(f: A => Option[B]): Stream[F, B] =
      fa.collect(Function.unlift(f))
  }
}
