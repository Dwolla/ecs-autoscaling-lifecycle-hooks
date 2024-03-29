package com.dwolla.aws.sns

import cats.*
import cats.syntax.all.*
import com.amazonaws.sns.TopicARN
import com.dwolla.aws.autoscaling.*
import feral.lambda.events.SnsMessage
import io.circe.*
import org.typelevel.log4cats.LoggerFactory

object ParseLifecycleHookNotification {
  def apply[F[_] : MonadThrow : LoggerFactory]: SnsMessage => F[Option[(TopicARN, LifecycleHookNotification)]] = s =>
    for {
      json <- io.circe.parser.parse(s.message).liftTo[F]
      autoScalingMessage <- json.as[AutoScalingSnsMessage].liftTo[F]
      notification: Option[LifecycleHookNotification] <- autoScalingMessage match {
        case a: LifecycleHookNotification =>
          a.some.pure[F]
        case TestNotification(_, requestId, _, _, _, _, _) =>
          LoggerFactory[F].create.flatMap(_.info(s"ignoring TestNotification message $requestId")).as(None)
      }
    } yield notification.tupleLeft(TopicARN(s.topicArn))
}
