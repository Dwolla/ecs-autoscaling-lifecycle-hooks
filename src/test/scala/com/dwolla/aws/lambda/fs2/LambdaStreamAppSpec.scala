package com.dwolla.aws.lambda.fs2

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import cats.effect._
import cats.implicits._
import cats.effect.concurrent.Deferred
import com.amazonaws.services.lambda.runtime._
import com.amazonaws.util.StringInputStream
import org.specs2.matcher._
import org.specs2.mutable.Specification
import org.scalacheck._
import org.scalacheck.Arbitrary._
import org.scalacheck.cats.implicits._
import org.specs2.ScalaCheck
import fs2._
import org.specs2.concurrent.ExecutionEnv
import org.specs2.execute._

import scala.concurrent.ExecutionContext
import scala.collection.JavaConverters._

class LambdaStreamAppSpec(implicit ee: ExecutionEnv) extends Specification with IOMatchers with Matchers with ScalaCheck {

  import LambdaContext._

  private implicit def ioAsResult[R: AsResult]: AsResult[IO[R]] = _.unsafeToFuture().await

  "LambdaStream" should {
    "read the given input and pass it to the implementation stream" >> prop { (arbitraryBytes: Array[Byte], arbitraryContext: Context) =>
      for {
        deferredBytes <- Deferred[IO, Array[Byte]]
        deferredContext <- Deferred[IO, Context]
        app = new LambdaStreamApp[IO] {
          override def run(context: Context, blockingExecutionContext: ExecutionContext)(stream: Stream[IO, Byte]): Stream[IO, Byte] =
            Stream.eval(for {
              _ <- deferredContext.complete(context)
              b <- stream.compile.to[Array]
              _ <- deferredBytes.complete(b)
            } yield 0.toByte)
        }
        inputStream = new ByteArrayInputStream(arbitraryBytes)
        outputStream = new ByteArrayOutputStream()
        _ <- IO(app.handleRequest(inputStream, outputStream, arbitraryContext))
        input <- deferredBytes.get
        passedContext <- deferredContext.get
      } yield {
        passedContext must be_==(arbitraryContext)
        outputStream.toByteArray must be_==(Array(0.toByte))
        input must be_==(arbitraryBytes)
      }
    }

    "write the contents of the transformed stream to the output stream" >> prop { (arbitraryBytes: Array[Byte], arbitraryContext: Context) =>
      val app = new LambdaStreamApp[IO] {
        override def run(context: Context, blockingExecutionContext: ExecutionContext)(stream: Stream[IO, Byte]): Stream[IO, Byte] =
          Stream.emits(arbitraryBytes)
      }
      val outputStream = new ByteArrayOutputStream()

      app.handleRequest(new StringInputStream("any-value"), outputStream, arbitraryContext)

      outputStream.toByteArray must be_==(arbitraryBytes)
    }

    "raise an exception if the stream fails" >> prop { arbitraryContext: Context =>
      val app = new LambdaStreamApp[IO] {
        override def run(context: Context, blockingExecutionContext: ExecutionContext)(stream: Stream[IO, Byte]): Stream[IO, Byte] =
          Stream.raiseError[IO](NoStackTraceException)
      }
      val outputStream = new ByteArrayOutputStream()

      app.handleRequest(new StringInputStream("any-value"), outputStream, arbitraryContext) must throwA[NoStackTraceException.type]
      outputStream.toByteArray must beEmpty
    }

    "raise an exception if the run method throws" >> prop { arbitraryContext: Context =>
      val app = new LambdaStreamApp[IO] {
        override def run(context: Context, blockingExecutionContext: ExecutionContext)(stream: Stream[IO, Byte]): Stream[IO, Byte] =
          throw NoStackTraceException
      }
      val outputStream = new ByteArrayOutputStream()

      app.handleRequest(new StringInputStream("any-value"), outputStream, arbitraryContext) must throwA[NoStackTraceException.type]
      outputStream.toByteArray must beEmpty
    }
  }

}

object LambdaContext {
  case class TestContext(awsRequestId: String,
                         logGroupName: String,
                         logStreamName: String,
                         functionName: String,
                         functionVersion: String,
                         invokedFunctionArn: String,
                         identity: CognitoIdentity,
                         clientContext: ClientContext,
                         remainingTimeInMillis: Int,
                         memoryLimitInMB: Int,
                         lambdaLogger: LambdaLogger,
                        ) extends Context {
    override def getAwsRequestId: String = awsRequestId
    override def getLogGroupName: String = logGroupName
    override def getLogStreamName: String = logStreamName
    override def getFunctionName: String = functionName
    override def getFunctionVersion: String = functionVersion
    override def getInvokedFunctionArn: String = invokedFunctionArn
    override def getIdentity: CognitoIdentity = identity
    override def getClientContext: ClientContext = clientContext
    override def getRemainingTimeInMillis: Int = remainingTimeInMillis
    override def getMemoryLimitInMB: Int = memoryLimitInMB
    override def getLogger: LambdaLogger = lambdaLogger
  }

  case class TestCognitoIdentity(identityId: String, identityPoolId: String) extends CognitoIdentity {
    override def getIdentityId: String = identityId
    override def getIdentityPoolId: String = identityPoolId
  }

  case class TestClientContext(client: Client, custom: Map[String, String], environment: Map[String, String]) extends ClientContext {
    override def getClient: Client = client
    override def getCustom: java.util.Map[String, String] = custom.asJava
    override def getEnvironment: java.util.Map[String, String] = environment.asJava
  }

  case class TestClient(installationId: String,
                        appTitle: String,
                        appVersionName: String,
                        appVersionCode: String,
                        appPackageName: String,
                       ) extends Client {
    override def getInstallationId: String = installationId
    override def getAppTitle: String = appTitle
    override def getAppVersionName: String = appVersionName
    override def getAppVersionCode: String = appVersionCode
    override def getAppPackageName: String = appPackageName
  }

  implicit val arbitraryLambdaLogger: Arbitrary[LambdaLogger] = Arbitrary(Gen.const[LambdaLogger](_ => ()))
  implicit val arbitraryContext: Arbitrary[Context] = Arbitrary(Gen.resultOf(TestContext.apply _).widen)
  implicit val arbitraryCognitoIdentity: Arbitrary[CognitoIdentity] = Arbitrary(Gen.resultOf(TestCognitoIdentity.apply _).widen)
  implicit val arbitraryClientContext: Arbitrary[ClientContext] = Arbitrary(Gen.resultOf(TestClientContext.apply _).widen)
  implicit val arbitraryClient: Arbitrary[Client] = Arbitrary(Gen.resultOf(TestClient.apply _).widen)
}

case object NoStackTraceException extends RuntimeException("exception deliberately thrown by test", null, true, false)
