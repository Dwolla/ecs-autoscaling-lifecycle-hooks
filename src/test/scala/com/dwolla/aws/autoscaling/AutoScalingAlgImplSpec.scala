package com.dwolla.aws.autoscaling

import java.util.concurrent.Future

import cats.effect._
import cats.effect.concurrent.{Deferred, MVar}
import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.services.autoscaling.AmazonAutoScalingAsync
import com.amazonaws.services.autoscaling.model._
import com.amazonaws.services.sns.AmazonSNSAsync
import com.amazonaws.services.sns.model.{PublishRequest, PublishResult}
import com.dwolla.NoOpLogger
import com.dwolla.aws.ArbitraryInstances._
import com.dwolla.aws.autoscaling.model.LifecycleHookNotification
import com.dwolla.aws.sns.FakeSNSAsync
import com.dwolla.aws.sns.model.SnsTopicArn
import io.circe.syntax._
import org.specs2.ScalaCheck
import org.specs2.matcher.{IOImplicits, IOMatchers}
import org.specs2.mutable.Specification

import scala.concurrent.duration._

class AutoScalingAlgImplSpec extends Specification with ScalaCheck with IOMatchers with IOImplicits {

  implicit val logger = NoOpLogger[IO]

  "AutoScalingAlgImpl" should {

    "make an async completeLifecycleAction request to continueAutoScaling" >> prop { arbLifecycleHookNotification: LifecycleHookNotification =>

      for {
        deferredCompleteLifecycleActionRequest <- Deferred[IO, CompleteLifecycleActionRequest]
        autoScalingClient <- IO.asyncF[AmazonAutoScalingAsync] { completeMock =>
          IO.async[CompleteLifecycleActionRequest] { completeRequest =>
            completeMock(Right(new FakeAutoScalingAsync {
              override def completeLifecycleActionAsync(req: CompleteLifecycleActionRequest,
                                                        asyncHandler: AsyncHandler[CompleteLifecycleActionRequest, CompleteLifecycleActionResult]): Future[CompleteLifecycleActionResult] = {
                completeRequest(Right(req))
                asyncHandler.onSuccess(req, null)
                null
              }
            }))
          }.flatMap(deferredCompleteLifecycleActionRequest.complete)
        }

        _ <- new AutoScalingAlgImpl[IO](autoScalingClient, null).continueAutoScaling(arbLifecycleHookNotification)

        passedReq <- deferredCompleteLifecycleActionRequest.get
      } yield {
        passedReq must beLike {
          case req: CompleteLifecycleActionRequest =>
            req.getLifecycleHookName must be_==(arbLifecycleHookNotification.lifecycleHookName)
            req.getAutoScalingGroupName must be_==(arbLifecycleHookNotification.autoScalingGroupName)
            req.getLifecycleActionResult must be_==("CONTINUE")
            req.getInstanceId must be_==(arbLifecycleHookNotification.EC2InstanceId)
        }
      }
    }

    "pause 5 seconds and then send a message to restart the lambda" >> prop { (arbSnsTopicArn: SnsTopicArn, arbLifecycleHookNotification: LifecycleHookNotification) =>
      val context = cats.effect.laws.util.TestContext()

      for {
        mvarPublishRequest <- MVar.empty[IO, PublishRequest]
        snsClient <- IO.asyncF[AmazonSNSAsync] { completeMock =>
          IO.async[PublishRequest] { completeRequest =>
            completeMock(Right(new FakeSNSAsync {
              override def publishAsync(req: PublishRequest,
                                        asyncHandler: AsyncHandler[PublishRequest, PublishResult]): Future[PublishResult] = {
                completeRequest(Right(req))
                asyncHandler.onSuccess(req, new PublishResult())
                null
              }
            }))
          }.flatMap(mvarPublishRequest.put)
        }

        cut = new AutoScalingAlgImpl[IO](null, snsClient)(Async[IO], context.timer[IO], NoOpLogger[IO])
        fiber <- cut.pauseAndRecurse(arbSnsTopicArn, arbLifecycleHookNotification).attempt.start(context.contextShift[IO])

        _ <- IO(context.tick(4.seconds))

        firstIsEmpty <- mvarPublishRequest.isEmpty

        _ <- IO(context.tick(1.seconds))

        success <- fiber.join
        publishedRequest <- mvarPublishRequest.read
      } yield {
        firstIsEmpty must beTrue
        success must beRight(())
        publishedRequest must beLikeA[PublishRequest] {
          case req =>
            req.getTopicArn must be_==(arbSnsTopicArn)
            req.getMessage must be_==(arbLifecycleHookNotification.asJson.noSpaces)
        }
      }
    }
  }
}
