package com.dwolla.aws.lambda

import cats.effect._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

package object fs2 {
  implicit def logger[F[_] : Sync]: Logger[F] = Slf4jLogger.getLoggerFromName[F]("LambdaLogger")
}
