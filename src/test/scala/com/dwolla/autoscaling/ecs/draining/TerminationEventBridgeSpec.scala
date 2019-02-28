package com.dwolla.autoscaling.ecs.draining

import cats.effect._
import cats.effect.concurrent.Deferred
import com.dwolla.NoOpLogger
import com.dwolla.aws.ArbitraryInstances._
import com.dwolla.aws.autoscaling._
import com.dwolla.aws.autoscaling.model.LifecycleHookNotification
import com.dwolla.aws.ec2.model.Ec2InstanceId
import com.dwolla.aws.ecs._
import com.dwolla.aws.ecs.model._
import com.dwolla.aws.sns.model.SnsTopicArn
import org.specs2.ScalaCheck
import org.specs2.matcher.{IOImplicits, IOMatchers}
import org.specs2.mutable.Specification

class TerminationEventBridgeSpec extends Specification with ScalaCheck with IOMatchers with IOImplicits {

  implicit val logger = NoOpLogger[IO]

  "TerminationEventBridge" should {
    "mark a non-draining instance as draining and pause and recurse" >> {
      prop { (arbSnsTopicArn: SnsTopicArn, arbLifecycleHookNotification: LifecycleHookNotification, arbClusterArn: ClusterArn, arbConInstId: ContainerInstanceId) =>
        for {
          deferredDrainInstanceArgs <- Deferred[IO, (ClusterArn, ContainerInstance)]
          deferredPauseAndRecurse <- Deferred[IO, (SnsTopicArn, LifecycleHookNotification)]
          expectedContainerInstance = ContainerInstance(arbConInstId, arbLifecycleHookNotification.EC2InstanceId, 1.asInstanceOf[TaskCount], ContainerStatus.Active)

          ecsAlg = new TestEcsAlg {
            override def findEc2Instance(ec2InstanceId: Ec2InstanceId) =
              IO.pure(Option((arbClusterArn, expectedContainerInstance)))

            override def drainInstanceImpl(cluster: ClusterArn, ci: ContainerInstance): IO[Unit] = deferredDrainInstanceArgs.complete((cluster, ci))
          }

          autoScalingAlg = new TestAutoScalingAlg {
            override def pauseAndRecurse(topic: SnsTopicArn, lifecycleHookNotification: LifecycleHookNotification): IO[Unit] =
              deferredPauseAndRecurse.complete((topic, lifecycleHookNotification))
          }

          _ <- new TerminationEventBridge(ecsAlg, autoScalingAlg).apply(arbSnsTopicArn, arbLifecycleHookNotification)

          (drainedClusterId, drainedContainerInstance) <- deferredDrainInstanceArgs.get
          (topic, lifecycleHook) <- deferredPauseAndRecurse.get
        } yield {
          drainedClusterId must be_==(arbClusterArn)
          drainedContainerInstance must be_==(ContainerInstance(arbConInstId, arbLifecycleHookNotification.EC2InstanceId, 1.asInstanceOf[TaskCount], ContainerStatus.Active))
          topic must be_==(arbSnsTopicArn)
          lifecycleHook must be_==(arbLifecycleHookNotification)
      }}
    }

    "pause and recurse if a draining instance still has tasks" >> {
      prop { (arbSnsTopicArn: SnsTopicArn, arbLifecycleHookNotification: LifecycleHookNotification, arbClusterArn: ClusterArn, arbConInstId: ContainerInstanceId) =>
        for {
          deferredPauseAndRecurse <- Deferred[IO, (SnsTopicArn, LifecycleHookNotification)]
          expectedContainerInstance = ContainerInstance(arbConInstId, arbLifecycleHookNotification.EC2InstanceId, 1.asInstanceOf[TaskCount], ContainerStatus.Draining)

          ecsAlg = new TestEcsAlg {
            override def findEc2Instance(ec2InstanceId: Ec2InstanceId) =
              IO.pure(Option((arbClusterArn, expectedContainerInstance)))
          }

          autoScalingAlg = new TestAutoScalingAlg {
            override def pauseAndRecurse(topic: SnsTopicArn, lifecycleHookNotification: LifecycleHookNotification): IO[Unit] =
              deferredPauseAndRecurse.complete((topic, lifecycleHookNotification))
          }

          _ <- new TerminationEventBridge(ecsAlg, autoScalingAlg).apply(arbSnsTopicArn, arbLifecycleHookNotification)

          (topic, lifecycleHook) <- deferredPauseAndRecurse.get
        } yield {
          topic must be_==(arbSnsTopicArn)
          lifecycleHook must be_==(arbLifecycleHookNotification)
        }
      }
    }

    "continue autoscaling if instance has no running tasks" >> {
      prop { (arbSnsTopicArn: SnsTopicArn, arbLifecycleHookNotification: LifecycleHookNotification, arbClusterArn: ClusterArn, arbConInstId: ContainerInstanceId) =>
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

          _ <- new TerminationEventBridge(ecsAlg, autoScalingAlg).apply(arbSnsTopicArn, arbLifecycleHookNotification)

          continuedLifecycleHook <- deferredLifecycleHookNotification.get
        } yield {
          continuedLifecycleHook must be_==(arbLifecycleHookNotification)
        }
      }
    }
  }

}
