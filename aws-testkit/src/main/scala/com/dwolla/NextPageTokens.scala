package com.dwolla

object NextPageTokens {
  def tokenForIdx(idx: Long, lastPage: Int): Option[String] =
    if (idx == 0 || idx == lastPage) None
    else Option(s"page-$idx")

  def tokenForIdx(idx: Int, lastPage: Int): Option[String] =
    tokenForIdx(idx.toLong, lastPage)
}
