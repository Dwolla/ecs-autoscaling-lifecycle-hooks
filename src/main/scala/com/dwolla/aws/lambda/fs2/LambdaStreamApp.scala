package com.dwolla.aws.lambda.fs2

import java.io.{InputStream, OutputStream}
import java.util.concurrent.Executors

import cats.effect._
import fs2._
import fs2.io._
import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

abstract class LambdaStreamApp[F[_] : Effect : ContextShift] extends RequestStreamHandler {
  protected val readChunkSize: Int = 4096

  def run(context: Context, blockingExecutionContext: ExecutionContext)(stream: Stream[F, Byte]): Stream[F, Byte]

  protected val blockingExecutionContext: Resource[F, ExecutionContextExecutorService] =
    Resource.make(Sync[F].delay(ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())))(ec => Sync[F].delay(ec.shutdown()))

  def go(input: InputStream, output: OutputStream, context: Context): Stream[F, Unit] =
    for {
      ec <- Stream.resource(blockingExecutionContext)
      () <- readInputStream(Sync[F].delay(input), readChunkSize, ec)
          .through(run(context, ec))
          .to(writeOutputStream(Sync[F].delay(output), ec))
    } yield ()

  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit =
    Effect[F].toIO {
      go(input, output, context)
        .compile
        .drain
    }
      .unsafeRunSync()

}
