package com.dwolla

import monix.newtypes.NewtypeWrapped

type NextPageToken = NextPageToken.Type
object NextPageToken extends NewtypeWrapped[Option[String]]

object NextPageTokens {
  def tokenForIdx(idx: Long, lastPage: Int): NextPageToken =
    NextPageToken(Option.unless(idx == 0 || idx == lastPage)(s"page-$idx"))

  def tokenForIdx(idx: Int, lastPage: Int): NextPageToken =
    tokenForIdx(idx.toLong, lastPage)
}
