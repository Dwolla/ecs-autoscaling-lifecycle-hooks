package com.dwolla
package aws

import cats.*
import cats.syntax.all.*
import monix.newtypes.HasExtractor

object ArbitraryPagination {
  def paginate[F[_], A](pages: List[F[A]])
                       (using Functor[F]): Map[NextPageToken, (F[A], NextPageToken)] =
    paginateWith(pages)(identity)

  def paginateWith[F[_], A, B, C](pages: A)
                                 (transform: B => C)
                                 (using Functor[F], HasExtractor.Aux[A, List[F[B]]]): Map[NextPageToken, (F[C], NextPageToken)] =
    paginateWith(implicitly[HasExtractor.Aux[A, List[F[B]]]].extract(pages))(transform)

  def paginateWith[F[_], A, B](pages: List[F[A]])
                              (transform: A => B)
                              (using Functor[F]): Map[NextPageToken, (F[B], NextPageToken)] =
    pages
      .zipWithIndex
      .map {
        case (page, index) =>
          val key = NextPageTokens.tokenForIdx(index, pages.size)
          val nextToken = NextPageTokens.tokenForIdx(index + 1, pages.size)
          val transformed = page.map(transform)

          key -> (transformed -> nextToken)
      }
      .toMap

}
