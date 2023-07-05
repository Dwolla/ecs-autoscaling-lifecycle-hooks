package com.dwolla.aws.autoscaling.model

import cats.syntax.all.*
import com.dwolla.aws.AccountId
import com.dwolla.aws.ec2.model.Ec2InstanceId
import io.circe.*
import monix.newtypes.integrations.*

import java.time.Instant

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

object LifecycleHookNotification extends DerivedCirceCodec {
  implicit val lifecycleHookNotificationEncoder: Encoder[LifecycleHookNotification] =
    Encoder.forProduct10(
      "Service",
      "Time",
      "RequestId",
      "LifecycleActionToken",
      "AccountId",
      "AutoScalingGroupName",
      "LifecycleHookName",
      "EC2InstanceId",
      "LifecycleTransition",
      "NotificationMetadata",
    ) { a =>
      (a.service,
        a.time,
        a.requestId,
        a.lifecycleActionToken,
        a.accountId,
        a.autoScalingGroupName,
        a.lifecycleHookName,
        a.EC2InstanceId,
        a.lifecycleTransition,
        a.notificationMetadata
      )
    }
  implicit val lifecycleHookNotificationDecoder: Decoder[LifecycleHookNotification] =
    Decoder.forProduct10(
      "Service",
      "Time",
      "RequestId",
      "LifecycleActionToken",
      "AccountId",
      "AutoScalingGroupName",
      "LifecycleHookName",
      "EC2InstanceId",
      "LifecycleTransition",
      "NotificationMetadata",
    )(LifecycleHookNotification.apply)
}
object TestNotification extends DerivedCirceCodec {
  implicit val testNotificationEncoder: Encoder[TestNotification] =
    Encoder.forProduct7(
      "AccountId",
      "RequestId",
      "AutoScalingGroupARN",
      "AutoScalingGroupName",
      "Service",
      "Event",
      "Time",
    ) { x =>
      (x.accountId,
        x.requestId,
        x.autoScalingGroupARN,
        x.autoScalingGroupName,
        x.service,
        x.event,
        x.time,
      )
    }
  implicit val testNotificationDecoder: Decoder[TestNotification] =
    Decoder.forProduct7(
      "AccountId",
      "RequestId",
      "AutoScalingGroupARN",
      "AutoScalingGroupName",
      "Service",
      "Event",
      "Time",
    )(TestNotification.apply)
}

object AutoScalingSnsMessage {
  implicit val autoScalingSnsMessageDecoder: Decoder[AutoScalingSnsMessage] =
    Decoder[LifecycleHookNotification].widen[AutoScalingSnsMessage]
      .or(Decoder[TestNotification].widen[AutoScalingSnsMessage])
}
