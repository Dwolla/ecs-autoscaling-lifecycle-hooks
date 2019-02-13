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

}

package model {

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
                                      )

  object LifecycleHookNotification {
    implicit def taggedStringEncoder[T]: Encoder[String @@ T] = Encoder[String].narrow
    implicit def taggedStringDecoder[T]: Decoder[String @@ T] = Decoder[String].map(_.asInstanceOf[String @@ T])

    implicit val lifecycleHookNotificationEncoder: Encoder[LifecycleHookNotification] = deriveEncoder[LifecycleHookNotification]
    implicit val lifecycleHookNotificationDecoder: Decoder[LifecycleHookNotification] = deriveDecoder[LifecycleHookNotification]
  }
}
