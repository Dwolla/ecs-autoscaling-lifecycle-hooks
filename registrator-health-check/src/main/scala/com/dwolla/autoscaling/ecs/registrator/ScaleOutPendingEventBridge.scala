package com.dwolla.autoscaling.ecs.registrator

import cats.*
import cats.syntax.all.*
import com.dwolla.aws.*
import com.dwolla.aws.autoscaling.*
import com.dwolla.aws.autoscaling.AdvanceLifecycleHook.*
import com.dwolla.aws.autoscaling.LifecycleState.*
import com.dwolla.aws.cloudformation.*
import com.dwolla.aws.ec2.Ec2Alg
import com.dwolla.aws.ecs.EcsAlg
import com.dwolla.aws.ecs.*
import com.dwolla.aws.sns.SnsTopicArn
import mouse.all.*

class ScaleOutPendingEventBridge[F[_] : Monad, G[_]](ECS: EcsAlg[F, G],
                                                     AutoScaling: AutoScalingAlg[F],
                                                     EC2: Ec2Alg[F],
                                                     Cfn: CloudFormationAlg[F],
                                                    ) {
  def apply(topic: SnsTopicArn, lifecycleHook: LifecycleHookNotification): F[Unit] =
    ECS.findEc2Instance(lifecycleHook.EC2InstanceId)
      .liftOptionT
      .mproduct { case (cluster, ContainerInstance(containerInstanceId, ec2InstanceId, _, _)) =>
        EC2.getTagsForInstance(lifecycleHook.EC2InstanceId)
          .find(_.name == TagName("aws:cloudformation:stack-id"))
          .liftOptionT
          .flatMapF { case Tag(_, stackId) =>
            Cfn.physicalResourceIdFor(StackArn(stackId.value), LogicalResourceId("registratorTaskDefinition"))
          }
          .map(id => TaskDefinitionArn(id.value))
      }
      .semiflatMap[AdvanceLifecycleHook] { case ((cluster, ci), taskDefinition) =>
        ECS.isTaskDefinitionRunningOnInstance(cluster, ci, taskDefinition)
          .map {
            case true => ContinueAutoScaling
            case false => PauseAndRecurse
          }
      }
      .getOrElse(PauseAndRecurse) // EC2 instance doesn't exist in ECS cluster yet, so pause and try again later
      .flatMap {
        case PauseAndRecurse => AutoScaling.pauseAndRecurse(topic, lifecycleHook, PendingWait)
        case ContinueAutoScaling => AutoScaling.continueAutoScaling(lifecycleHook)
      }
}

object ScaleOutPendingEventBridge {
  def apply[F[_] : Monad, G[_]](ecsAlg: EcsAlg[F, G],
                                autoScalingAlg: AutoScalingAlg[F],
                                ec2Alg: Ec2Alg[F],
                                cloudFormationAlg: CloudFormationAlg[F],
                               ): (SnsTopicArn, LifecycleHookNotification) => F[Unit] =
    new ScaleOutPendingEventBridge(ecsAlg, autoScalingAlg, ec2Alg, cloudFormationAlg).apply
}

extension [F[_], G[_], A](fga: F[G[A]])(using Functor[F], Foldable[G]) {
  def find(pred: A => Boolean): F[Option[A]] =
    fga.map(_.find(pred))
}
