package com.dwolla.autoscaling.ecs.draining

import cats.effect._
import cats.effect.concurrent.Deferred
import com.dwolla.aws.ArbitraryInstances._
import com.dwolla.aws.autoscaling._
import com.dwolla.aws.autoscaling.model.LifecycleHookNotification
import com.dwolla.aws.ec2.model.Ec2InstanceId
import com.dwolla.aws.ecs._
import com.dwolla.aws.ecs.model._
import org.specs2.ScalaCheck
import org.specs2.matcher.{IOImplicits, IOMatchers}
import org.specs2.mutable.Specification

class TerminationEventBridgeSpec extends Specification with ScalaCheck with IOMatchers with IOImplicits {

  "TerminationEventBridge" should {
    "mark a non-draining instance as draining and pause and recurse" >> {
      prop { (arbLifecycleHookNotification: LifecycleHookNotification, arbClusterArn: ClusterArn, arbConInstId: ContainerInstanceId) =>
        for {
          deferredDrainInstanceArgs <- Deferred[IO, (ClusterArn, ContainerInstance)]
          deferredPauseAndRecurse <- Deferred[IO, Unit]
          expectedContainerInstance = ContainerInstance(arbConInstId, arbLifecycleHookNotification.EC2InstanceId, 1.asInstanceOf[TaskCount], ContainerStatus.Active)

          ecsAlg = new TestEcsAlg {
            override def findEc2Instance(ec2InstanceId: Ec2InstanceId) =
              IO.pure(Option((arbClusterArn, expectedContainerInstance)))

            override def drainInstanceImpl(cluster: ClusterArn, ci: ContainerInstance): IO[Unit] = deferredDrainInstanceArgs.complete((cluster, ci))
          }

          autoScalingAlg = new TestAutoScalingAlg {
            override def pauseAndRecurse: IO[Unit] = deferredPauseAndRecurse.complete(())
          }

          _ <- new TerminationEventBridge(ecsAlg, autoScalingAlg).apply(arbLifecycleHookNotification)

          (drainedClusterId, drainedContainerInstance) <- deferredDrainInstanceArgs.get
          () <- deferredPauseAndRecurse.get
        } yield {
          drainedClusterId must be_==(arbClusterArn)
          drainedContainerInstance must be_==(ContainerInstance(arbConInstId, arbLifecycleHookNotification.EC2InstanceId, 1.asInstanceOf[TaskCount], ContainerStatus.Active))
      }}
    }

    "pause and recurse if a draining instance still has tasks" >> {
      prop { (arbLifecycleHookNotification: LifecycleHookNotification, arbClusterArn: ClusterArn, arbConInstId: ContainerInstanceId) =>
        for {
          deferredPauseAndRecurse <- Deferred[IO, String]
          expectedContainerInstance = ContainerInstance(arbConInstId, arbLifecycleHookNotification.EC2InstanceId, 1.asInstanceOf[TaskCount], ContainerStatus.Draining)

          ecsAlg = new TestEcsAlg {
            override def findEc2Instance(ec2InstanceId: Ec2InstanceId) =
              IO.pure(Option((arbClusterArn, expectedContainerInstance)))
          }

          autoScalingAlg = new TestAutoScalingAlg {
            override def pauseAndRecurse: IO[Unit] = deferredPauseAndRecurse.complete("success")
          }

          _ <- new TerminationEventBridge(ecsAlg, autoScalingAlg).apply(arbLifecycleHookNotification)

          paused <- deferredPauseAndRecurse.get
        } yield {
          paused must be_==("success")
        }
      }
    }

    "continue autoscaling if instance has no running tasks" >> {
      prop { (arbLifecycleHookNotification: LifecycleHookNotification, arbClusterArn: ClusterArn, arbConInstId: ContainerInstanceId) =>
        for {
          deferredLifecycleHookNotification <- Deferred[IO, LifecycleHookNotification]
          expectedContainerInstance = ContainerInstance(arbConInstId, arbLifecycleHookNotification.EC2InstanceId, 0.asInstanceOf[TaskCount], ContainerStatus.Draining)

          ecsAlg = new TestEcsAlg {
            override def findEc2Instance(ec2InstanceId: Ec2InstanceId) =
              IO.pure(Option((arbClusterArn, expectedContainerInstance)))
          }

          autoScalingAlg = new TestAutoScalingAlg {
            override def continueAutoScaling(l: LifecycleHookNotification): IO[Unit] =
              deferredLifecycleHookNotification.complete(l)
          }

          _ <- new TerminationEventBridge(ecsAlg, autoScalingAlg).apply(arbLifecycleHookNotification)

          continuedLifecycleHook <- deferredLifecycleHookNotification.get
        } yield {
          continuedLifecycleHook must be_==(arbLifecycleHookNotification)
        }
      }
    }
  }

}
