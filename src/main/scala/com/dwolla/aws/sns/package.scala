package com.dwolla.aws

import cats.effect.{Resource, Sync}
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}

package object sns {
  def resource[F[_] : Sync]: Resource[F, AmazonSNSAsync] =
    Resource.make(Sync[F].delay(AmazonSNSAsyncClientBuilder.defaultClient()))(c => Sync[F].delay(c.shutdown()))
}
