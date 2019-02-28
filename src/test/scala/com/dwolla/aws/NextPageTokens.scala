package com.dwolla.aws

object NextPageTokens {
  import scala.language.reflectiveCalls

  def tokenForIdx(idx: Long, lastPage: Int): Option[String] =
    if (idx == 0 || idx == lastPage) None
    else Option(s"page-$idx")

  def tokenForIdx(idx: Int, lastPage: Int): Option[String] =
    tokenForIdx(idx.toLong, lastPage)

  implicit class AddNextToken[A <: { def withNextToken(token: String): A}](a: A) {
    def withNextToken(idx: Long, lastPage: Int): A =
      tokenForIdx(idx, lastPage).fold(a)(a.withNextToken)

    def withNextToken(idx: Int, lastPage: Int): A =
      withNextToken(idx.toLong, lastPage)
  }
}
