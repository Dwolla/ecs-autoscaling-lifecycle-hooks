package com.dwolla.autoscaling.ecs.registrator

import cats.*
import cats.effect.*
import cats.syntax.all.*
import com.dwolla.aws.*
import com.dwolla.aws.autoscaling.*
import com.dwolla.aws.autoscaling.AdvanceLifecycleHook.*
import com.dwolla.aws.autoscaling.LifecycleState.PendingWait
import com.dwolla.aws.cloudformation.*
import com.dwolla.aws.ec2.*
import com.dwolla.aws.ecs.*
import com.dwolla.aws.sns.SnsTopicArn
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.effect.PropF.forAllF

class ScaleOutPendingEventBridgeSpec
  extends CatsEffectSuite
    with ScalaCheckEffectSuite
    with com.dwolla.aws.ArbitraryInstances {

  test("ScaleOutPendingEventBridge continues autoscaling when Registrator is running on instance") {
    forAllF { (arbTopic: SnsTopicArn,
               arbLifecycleHook: LifecycleHookNotification,
               arbCluster: ClusterArn,
               arbContainerInstance: ContainerInstance,
               arbStackArn: StackArn,
               arbTaskDefinitionArn: TaskDefinitionArn,
               arbTags: List[Tag],
               arbRegistratorStatus: Boolean,
              ) =>
      for {
        deferredResult <- Deferred[IO, (AdvanceLifecycleHook, Option[SnsTopicArn], LifecycleHookNotification, Option[LifecycleState])]
        ecs = new TestEcsAlg {
          override def findEc2Instance(ec2InstanceId: Ec2InstanceId): IO[Option[(ClusterArn, ContainerInstance)]] =
            Option.when(ec2InstanceId == arbLifecycleHook.EC2InstanceId) {
              (arbCluster, arbContainerInstance)
            }.pure[IO]

          override def isTaskDefinitionRunningOnInstance(cluster: ClusterArn,
                                                         ci: ContainerInstance,
                                                         taskDefinition: TaskDefinitionArn): IO[Boolean] =
            (arbRegistratorStatus &&
              cluster == arbCluster &&
              ci == arbContainerInstance &&
              taskDefinition == arbTaskDefinitionArn).pure[IO]
        }
        autoscaling = new FakeAutoScalingAlgThatCapturesMethodParameters(deferredResult)
        ec2 = new FakeEc2AlgThatReturnsArbitraryTagsWithCloudFormationStackIdTag(arbLifecycleHook, arbStackArn, arbTags)
        cloudformation = new TestCloudFormationAlg {
          override def physicalResourceIdFor(stack: StackArn,
                                             logicalResourceId: LogicalResourceId): IO[Option[PhysicalResourceId]] =
            Option.when(stack == arbStackArn && logicalResourceId == LogicalResourceId("registratorTaskDefinition")) {
              PhysicalResourceId(arbTaskDefinitionArn.value)
            }.pure[IO]
        }

        _ <- new ScaleOutPendingEventBridge(ecs, autoscaling, ec2, cloudformation).apply(arbTopic, arbLifecycleHook)

        (result, maybeTopic, capturedLifecycleHookNotification, maybeGuardState) <- deferredResult.get

      } yield {
        assertEquals(result, if (arbRegistratorStatus) ContinueAutoScaling else PauseAndRecurse)
        assertEquals(maybeTopic, Option.unless(arbRegistratorStatus)(arbTopic))
        assertEquals(capturedLifecycleHookNotification, arbLifecycleHook)
        assertEquals(maybeGuardState, Option.unless(arbRegistratorStatus)(PendingWait))
      }
    }
  }

  test("ScaleOutPendingEventBridge pauses and recurses if EC2 instance isn't found in ECS cluster") {
    forAllF { (arbTopic: SnsTopicArn,
               arbLifecycleHook: LifecycleHookNotification,
               arbCluster: ClusterArn,
               arbContainerInstance: ContainerInstance,
               arbStackArn: StackArn,
               arbTaskDefinitionArn: TaskDefinitionArn,
               arbTags: List[Tag],
              ) =>
      for {
        deferredResult <- Deferred[IO, (AdvanceLifecycleHook, Option[SnsTopicArn], LifecycleHookNotification, Option[LifecycleState])]
        ecs = new TestEcsAlg {
          override def findEc2Instance(ec2InstanceId: Ec2InstanceId): IO[Option[(ClusterArn, ContainerInstance)]] =
            none[(ClusterArn, ContainerInstance)].pure[IO]
        }
        autoscaling = new FakeAutoScalingAlgThatCapturesMethodParameters(deferredResult)
        ec2 = new FakeEc2AlgThatReturnsArbitraryTagsWithCloudFormationStackIdTag(arbLifecycleHook, arbStackArn, arbTags)
        cloudformation = new TestCloudFormationAlg {}
        _ <- new ScaleOutPendingEventBridge(ecs, autoscaling, ec2, cloudformation).apply(arbTopic, arbLifecycleHook)

        (result, maybeTopic, capturedLifecycleHookNotification, maybeGuardState) <- deferredResult.get

      } yield {
        assertEquals(result, PauseAndRecurse)
        assertEquals(maybeTopic, arbTopic.some)
        assertEquals(capturedLifecycleHookNotification, arbLifecycleHook)
        assertEquals(maybeGuardState, PendingWait.some)
      }
    }
  }
}

class FakeEc2AlgThatReturnsArbitraryTagsWithCloudFormationStackIdTag(arbLifecycleHook: LifecycleHookNotification,
                                                                     arbStackArn: StackArn,
                                                                     arbTags: List[Tag],
                                                                    ) extends TestEc2Alg {
  override def getTagsForInstance(id: Ec2InstanceId): IO[List[Tag]] =
    IO.pure {
      if (id == arbLifecycleHook.EC2InstanceId) arbTags ++ List(
        Tag(TagName("aws:cloudformation:stack-id"), TagValue(arbStackArn.value)),
      )
      else List.empty
    }
}

class FakeAutoScalingAlgThatCapturesMethodParameters(deferredResult: Deferred[IO, (AdvanceLifecycleHook, Option[SnsTopicArn], LifecycleHookNotification, Option[LifecycleState])]) extends TestAutoScalingAlg {
  override def pauseAndRecurse(topic: SnsTopicArn, 
                               lifecycleHookNotification: LifecycleHookNotification,
                               onlyIfInState: LifecycleState,
                              ): IO[Unit] =
    deferredResult.complete((PauseAndRecurse, topic.some, lifecycleHookNotification, onlyIfInState.some)).void

  override def continueAutoScaling(l: LifecycleHookNotification): IO[Unit] =
    deferredResult.complete((ContinueAutoScaling, None, l, None)).void
}
