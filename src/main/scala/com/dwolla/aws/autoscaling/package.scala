package com.dwolla.aws

import cats.effect._
import com.amazonaws.services.autoscaling.{AmazonAutoScalingAsync, AmazonAutoScalingAsyncClientBuilder}

package object autoscaling {

  def resource[F[_] : Sync]: Resource[F, AmazonAutoScalingAsync] =
      Resource.make(Sync[F].delay(AmazonAutoScalingAsyncClientBuilder.defaultClient()))(c => Sync[F].delay(c.shutdown()))

}
