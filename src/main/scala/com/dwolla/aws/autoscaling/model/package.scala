package com.dwolla.aws.autoscaling

import java.time.Instant

import cats.implicits._
import com.dwolla.aws.AccountId
import com.dwolla.aws.ec2.model.Ec2InstanceId
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import shapeless.tag.@@

package object model {

  private[model] implicit val config: Configuration = Configuration(
    transformMemberNames = _.capitalize,
    transformConstructorNames = _.capitalize,
    useDefaults = true,
    discriminator = None,
  )

  private[model] implicit def taggedStringEncoder[T]: Encoder[String @@ T] = Encoder[String].narrow
  private[model] implicit def taggedStringDecoder[T]: Decoder[String @@ T] = Decoder[String].map(_.asInstanceOf[String @@ T])

}

package model {

  sealed trait AutoScalingSnsMessage
  case class LifecycleHookNotification(service: String,
                                       time: Instant,
                                       requestId: String,
                                       lifecycleActionToken: String,
                                       accountId: AccountId,
                                       autoScalingGroupName: String,
                                       lifecycleHookName: String,
                                       EC2InstanceId: Ec2InstanceId,
                                       lifecycleTransition: String,
                                       notificationMetadata: Option[String],
                                      ) extends AutoScalingSnsMessage
  case class TestNotification(accountId: AccountId,
                              requestId: String,
                              autoScalingGroupARN: String,
                              autoScalingGroupName: String,
                              service: String,
                              event: String,
                              time: Instant,
                             ) extends AutoScalingSnsMessage


  object LifecycleHookNotification {
    implicit val lifecycleHookNotificationEncoder: Encoder[LifecycleHookNotification] = deriveEncoder[LifecycleHookNotification]
    implicit val lifecycleHookNotificationDecoder: Decoder[LifecycleHookNotification] = deriveDecoder[LifecycleHookNotification]
  }
  object TestNotification {
    implicit val testNotificationEncoder: Encoder[TestNotification] = deriveEncoder[TestNotification]
    implicit val testNotificationDecoder: Decoder[TestNotification] = deriveDecoder[TestNotification]
  }

  object AutoScalingSnsMessage {
    implicit val autoScalingSnsMessageDecoder: Decoder[AutoScalingSnsMessage] =
      Decoder[LifecycleHookNotification].widen[AutoScalingSnsMessage]
        .or(Decoder[TestNotification].widen[AutoScalingSnsMessage])
  }
}
