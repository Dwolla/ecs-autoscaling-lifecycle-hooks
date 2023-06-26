package feral.lambda

import cats.*
import cats.syntax.all.*
import io.circe.JsonObject
import io.circe.testing.instances.*
import org.scalacheck.*
import org.scalacheck.Arbitrary.arbitrary

import scala.concurrent.duration.*

trait ContextInstances {
  def genContext[F[_] : Applicative]: Gen[Context[F]] =
    for {
      functionName <- arbitrary[String]
      functionVersion <- arbitrary[String]
      invokedFunctionArn <- arbitrary[String]
      memoryLimitInMB <- arbitrary[Int]
      awsRequestId <- arbitrary[String]
      logGroupName <- arbitrary[String]
      logStreamName <- arbitrary[String]
      identity <- arbitrary[Option[CognitoIdentity]]
      clientContext <- arbitrary[Option[ClientContext]]
      remainingTime <- arbitrary[FiniteDuration]
    } yield
      new Context(
        functionName,
        functionVersion,
        invokedFunctionArn,
        memoryLimitInMB,
        awsRequestId,
        logGroupName,
        logStreamName,
        identity,
        clientContext,
        remainingTime.pure[F],
      )

  implicit def arbContext[F[_] : Applicative]: Arbitrary[Context[F]] = Arbitrary(genContext)

  val genCognitoIdentity: Gen[CognitoIdentity] =
    for {
      identityId <- arbitrary[String]
      identityPoolId <- arbitrary[String]
    } yield new CognitoIdentity(identityId, identityPoolId)
  implicit val arbCognitoIdentity: Arbitrary[CognitoIdentity] = Arbitrary(genCognitoIdentity)

  val genClientContextClient: Gen[ClientContextClient] =
    for {
      installationId <- arbitrary[String]
      appTitle <- arbitrary[String]
      appVersionName <- arbitrary[String]
      appVersionCode <- arbitrary[String]
      appPackageName <- arbitrary[String]
    } yield new ClientContextClient(installationId, appTitle, appVersionName, appVersionCode, appPackageName)
  implicit val arbClientContextClient: Arbitrary[ClientContextClient] = Arbitrary(genClientContextClient)

  val genClientContextEnv: Gen[ClientContextEnv] =
    for {
      platformVersion <- arbitrary[String]
      platform <- arbitrary[String]
      make <- arbitrary[String]
      model <- arbitrary[String]
      locale <- arbitrary[String]
    } yield new ClientContextEnv(platformVersion, platform, make, model, locale)
  implicit val arbClientContextEnv: Arbitrary[ClientContextEnv] = Arbitrary(genClientContextEnv)

  val genClientContext: Gen[ClientContext] =
    for {
      client <- arbitrary[ClientContextClient]
      env <- arbitrary[ClientContextEnv]
      custom <- arbitrary[JsonObject]
    } yield new ClientContext(client, env, custom)
  implicit val arbClientContext: Arbitrary[ClientContext] = Arbitrary(genClientContext)
}

object TestContext extends ContextInstances {
  def empty[F[_] : Applicative]: Context[F] =
    new Context(
      "",
      "",
      "",
      Int.MaxValue,
      "",
      "",
      "",
      None,
      None,
      1.minute.pure[F],
    )
}
