package com.dwolla.autoscaling.ecs.draining

import cats.*
import cats.syntax.all.*
import com.dwolla.aws.autoscaling.model.{AutoScalingSnsMessage, LifecycleHookNotification, TestNotification}
import com.dwolla.aws.sns.model.SnsTopicArn
import feral.lambda.events.SnsMessage
import org.typelevel.log4cats.LoggerFactory

object ParseLifecycleHookNotification {
  def apply[F[_] : MonadThrow : LoggerFactory]: SnsMessage => F[Option[(SnsTopicArn, LifecycleHookNotification)]] = s =>
    for {
      json <- io.circe.parser.parse(s.message).liftTo[F]
      autoScalingMessage <- json.as[AutoScalingSnsMessage].liftTo[F]
      notification: Option[LifecycleHookNotification] <- autoScalingMessage match {
        case a: LifecycleHookNotification =>
          a.some.pure[F]
        case TestNotification(_, requestId, _, _, _, _, _) =>
          LoggerFactory[F].create.flatMap(_.info(s"ignoring TestNotification message $requestId")).as(None)
      }
    } yield notification.tupleLeft(SnsTopicArn(s.topicArn))
}
