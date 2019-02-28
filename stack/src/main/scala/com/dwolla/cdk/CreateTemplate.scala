package com.dwolla.cdk

import java.io.File
import java.nio.file.Path
import java.util.concurrent.Executors

import _root_.fs2._
import _root_.fs2.io.file.writeAll
import _root_.io.circe._
import _root_.io.circe.syntax._
import cats.effect._
import cats.implicits._
import software.amazon.awscdk.{App => AwsApp}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

object CreateTemplate extends IOApp {

  private val blockingExecutionContext: Resource[IO, ExecutionContextExecutorService] =
    Resource.make(IO(ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())))(ec => IO(ec.shutdown()))

  private def outputFile(filename: String): IO[Path] = IO {
    new File(filename).toPath
  }

  private def stream(filename: String): Stream[IO, Unit] =
    for {
      ec <- Stream.resource(blockingExecutionContext)
      path <- Stream.eval(outputFile(filename))
      _ <- Stream.eval(template).through(text.utf8Encode).to(writeAll(path, ec))
    } yield ()

  private def template: IO[String] =
    for {
      app <- IO(new AwsApp)
      stackName = "ProdAutoScalingEcsDrainingLambda"
      _ <- AutoScalingEcsDrainingLambdaStack[IO](app, stackName)
      json <- IO {
        JsonifyHashMap(app.synthesizeStack(stackName).getTemplate.asInstanceOf[java.util.HashMap[String, AnyRef]]).asJson
      }
    } yield json.spaces2

  override def run(args: List[String]): IO[ExitCode] =
    stream(args.head).compile.drain.as(ExitCode.Success)
}

object JsonifyHashMap {
  import scala.collection.JavaConverters._

  type JavaMap = java.util.HashMap[_, _]
  type JavaList = java.util.List[_]

  // this is gross 🙅‍🤢🤮
  def apply(hm: JavaMap): JsonObject = {
    val entries = hm.entrySet().asScala.map { kv =>
      val k = kv.getKey.asInstanceOf[String]
      val v = kv.getValue

      v match {
        case map: JavaMap => k -> JsonifyHashMap(map).asJson
        case list: JavaList => k -> JsonifyHashMap(list)
        case str: String => k -> str.asJson
        case i: Int => k -> i.asJson
        case other => throw new RuntimeException(s"Found $other but don't know how to Jsonify it yet, please teach me")
      }
    }.toList

    JsonObject(entries: _*)
  }

  def apply(items: JavaList): Json = {
    val x = items.asScala.map {
      case s: String => s.asJson
      case hm: JavaMap => JsonifyHashMap(hm).asJson
    }

    Json.arr(x: _*)
  }
}
