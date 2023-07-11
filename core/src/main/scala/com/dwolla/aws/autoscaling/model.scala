package com.dwolla.aws.autoscaling

import cats.FlatMap
import cats.syntax.all.*
import com.dwolla.aws.AccountId
import com.dwolla.aws.ec2.Ec2InstanceId
import io.circe.*
import monix.newtypes.NewtypeWrapped
import monix.newtypes.integrations.*

import java.time.Instant

type AutoScalingGroupName = AutoScalingGroupName.Type
object AutoScalingGroupName extends NewtypeWrapped[String]

type LifecycleHookName = LifecycleHookName.Type
object LifecycleHookName extends NewtypeWrapped[String]

type LifecycleTransition = LifecycleTransition.Type
object LifecycleTransition extends NewtypeWrapped[String]

sealed trait AutoScalingSnsMessage
case class LifecycleHookNotification(service: String,
                                     time: Instant,
                                     requestId: String,
                                     lifecycleActionToken: String,
                                     accountId: AccountId,
                                     autoScalingGroupName: AutoScalingGroupName,
                                     lifecycleHookName: LifecycleHookName,
                                     EC2InstanceId: Ec2InstanceId,
                                     lifecycleTransition: LifecycleTransition,
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

enum AdvanceLifecycleHook {
  case PauseAndRecurse
  case ContinueAutoScaling
}

object AdvanceLifecycleHook {
  extension [F[_]](cas: F[AdvanceLifecycleHook])(using FlatMap[F]) {
    def fold[B](ifPause: => F[B], ifContinue: => F[B]): F[B] =
      cas.flatMap {
        case PauseAndRecurse => ifPause
        case ContinueAutoScaling => ifContinue
      }
  }
}

enum LifecycleState(val awsName: String) {
  case PendingWait extends LifecycleState("Pending:Wait")
  case PendingProceed extends LifecycleState("Pending:Proceed")
  case InService extends LifecycleState("InService")
  case TerminatingWait extends LifecycleState("Terminating:Wait")
  case TerminatingProceed extends LifecycleState("Terminating:Proceed")
}

object LifecycleState {
  private val maybeFromString: PartialFunction[String, LifecycleState] = {
    case "Pending:Wait" => PendingWait
    case "Pending:Proceed" => PendingProceed
    case "InService" => InService
    case "Terminating:Wait" => TerminatingWait
    case "Terminating:Proceed" => TerminatingProceed
  }
  
  def fromString(s: String): Option[LifecycleState] = maybeFromString.lift(s)
}
