package com.dwolla.autoscaling.ecs.draining

import cats.effect.*
import com.dwolla.aws.autoscaling.LifecycleState.TerminatingWait
import com.dwolla.aws.autoscaling.{*, given}
import com.dwolla.aws.ec2.Ec2InstanceId
import com.dwolla.aws.ecs.{*, given}
import com.dwolla.aws.sns.{SnsTopicArn, given}
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.effect.PropF.forAllF

class TerminationEventBridgeSpec 
  extends CatsEffectSuite 
    with ScalaCheckEffectSuite {

  test("TerminationEventBridge should mark a non-draining instance as draining and pause and recurse") {
    forAllF { (arbSnsTopicArn: SnsTopicArn, 
               arbLifecycleHookNotification: LifecycleHookNotification, 
               arbClusterArn: ClusterArn, 
               arbConInstId: ContainerInstanceId) =>
      for {
        deferredDrainInstanceArgs <- Deferred[IO, (ClusterArn, ContainerInstance)]
        deferredPauseAndRecurse <- Deferred[IO, (SnsTopicArn, LifecycleHookNotification, LifecycleState)]
        expectedContainerInstance = ContainerInstance(arbConInstId, arbLifecycleHookNotification.EC2InstanceId, 1.asInstanceOf[TaskCount], ContainerInstanceStatus.Active)

        ecsAlg = new TestEcsAlg {
          override def findEc2Instance(ec2InstanceId: Ec2InstanceId) =
            IO.pure(Option((arbClusterArn, expectedContainerInstance)))

          override def drainInstanceImpl(cluster: ClusterArn, ci: ContainerInstance): IO[Unit] = deferredDrainInstanceArgs.complete((cluster, ci)).void
        }

        autoScalingAlg = new TestAutoScalingAlg {
          override def pauseAndRecurse(topic: SnsTopicArn,
                                       lifecycleHookNotification: LifecycleHookNotification,
                                       onlyIfInState: LifecycleState,
                                      ): IO[Unit] =
            deferredPauseAndRecurse.complete((topic, lifecycleHookNotification, onlyIfInState)).void
        }

        _ <- new TerminationEventBridge(ecsAlg, autoScalingAlg).apply(arbSnsTopicArn, arbLifecycleHookNotification)

        (drainedClusterId, drainedContainerInstance) <- deferredDrainInstanceArgs.get
        (topic, lifecycleHook, guardState) <- deferredPauseAndRecurse.get
      } yield {
        assertEquals(drainedClusterId, arbClusterArn)
        assertEquals(drainedContainerInstance, ContainerInstance(arbConInstId, arbLifecycleHookNotification.EC2InstanceId, 1.asInstanceOf[TaskCount], ContainerInstanceStatus.Active))
        assertEquals(topic, arbSnsTopicArn)
        assertEquals(lifecycleHook, arbLifecycleHookNotification)
        assertEquals(guardState, TerminatingWait)
      }
    }
  }

  test("TerminationEventBridge should pause and recurse if a draining instance still has tasks") {
    forAllF { (arbSnsTopicArn: SnsTopicArn, 
               arbLifecycleHookNotification: LifecycleHookNotification, 
               arbClusterArn: ClusterArn, 
               arbConInstId: ContainerInstanceId) =>
      for {
        deferredPauseAndRecurse <- Deferred[IO, (SnsTopicArn, LifecycleHookNotification, LifecycleState)]
        expectedContainerInstance = ContainerInstance(arbConInstId, arbLifecycleHookNotification.EC2InstanceId, 1.asInstanceOf[TaskCount], ContainerInstanceStatus.Draining)

        ecsAlg = new TestEcsAlg {
          override def findEc2Instance(ec2InstanceId: Ec2InstanceId) =
            IO.pure(Option((arbClusterArn, expectedContainerInstance)))
        }

        autoScalingAlg = new TestAutoScalingAlg {
          override def pauseAndRecurse(topic: SnsTopicArn,
                                       lifecycleHookNotification: LifecycleHookNotification,
                                       onlyIfInState: LifecycleState,
                                      ): IO[Unit] =
            deferredPauseAndRecurse.complete((topic, lifecycleHookNotification, onlyIfInState)).void
        }

        _ <- new TerminationEventBridge(ecsAlg, autoScalingAlg).apply(arbSnsTopicArn, arbLifecycleHookNotification)

        (topic, lifecycleHook, guardState) <- deferredPauseAndRecurse.get
      } yield {
        assertEquals(topic, arbSnsTopicArn)
        assertEquals(lifecycleHook, arbLifecycleHookNotification)
        assertEquals(guardState, TerminatingWait)
      }
    }
  }

  test("TerminationEventBridge should continue autoscaling if instance has no running tasks") {
    forAllF { (arbSnsTopicArn: SnsTopicArn, 
               arbLifecycleHookNotification: LifecycleHookNotification, 
               arbClusterArn: ClusterArn, 
               arbConInstId: ContainerInstanceId) =>
      for {
        deferredLifecycleHookNotification <- Deferred[IO, LifecycleHookNotification]
        expectedContainerInstance = ContainerInstance(arbConInstId, arbLifecycleHookNotification.EC2InstanceId, 0.asInstanceOf[TaskCount], ContainerInstanceStatus.Draining)

        ecsAlg = new TestEcsAlg {
          override def findEc2Instance(ec2InstanceId: Ec2InstanceId) =
            IO.pure(Option((arbClusterArn, expectedContainerInstance)))
        }

        autoScalingAlg = new TestAutoScalingAlg {
          override def continueAutoScaling(l: LifecycleHookNotification): IO[Unit] =
            deferredLifecycleHookNotification.complete(l).void
        }

        _ <- new TerminationEventBridge(ecsAlg, autoScalingAlg).apply(arbSnsTopicArn, arbLifecycleHookNotification)

        continuedLifecycleHook <- deferredLifecycleHookNotification.get
      } yield {
        assertEquals(continuedLifecycleHook, arbLifecycleHookNotification)
      }
    }
  }

}
