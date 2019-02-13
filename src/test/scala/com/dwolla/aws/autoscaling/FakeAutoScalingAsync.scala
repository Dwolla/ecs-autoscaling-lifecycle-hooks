package com.dwolla.aws.autoscaling

import java.util.concurrent.Future

import com.amazonaws.{AmazonWebServiceRequest, ResponseMetadata}
import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.regions.Region
import com.amazonaws.services.autoscaling.AmazonAutoScalingAsync
import com.amazonaws.services.autoscaling.model._
import com.amazonaws.services.autoscaling.waiters.AmazonAutoScalingWaiters

abstract class FakeAutoScalingAsync extends AmazonAutoScalingAsync {
  override def attachInstancesAsync(attachInstancesRequest: AttachInstancesRequest): Future[AttachInstancesResult] = ???

  override def attachInstancesAsync(attachInstancesRequest: AttachInstancesRequest, asyncHandler: AsyncHandler[AttachInstancesRequest, AttachInstancesResult]): Future[AttachInstancesResult] = ???

  override def attachLoadBalancerTargetGroupsAsync(attachLoadBalancerTargetGroupsRequest: AttachLoadBalancerTargetGroupsRequest): Future[AttachLoadBalancerTargetGroupsResult] = ???

  override def attachLoadBalancerTargetGroupsAsync(attachLoadBalancerTargetGroupsRequest: AttachLoadBalancerTargetGroupsRequest, asyncHandler: AsyncHandler[AttachLoadBalancerTargetGroupsRequest, AttachLoadBalancerTargetGroupsResult]): Future[AttachLoadBalancerTargetGroupsResult] = ???

  override def attachLoadBalancersAsync(attachLoadBalancersRequest: AttachLoadBalancersRequest): Future[AttachLoadBalancersResult] = ???

  override def attachLoadBalancersAsync(attachLoadBalancersRequest: AttachLoadBalancersRequest, asyncHandler: AsyncHandler[AttachLoadBalancersRequest, AttachLoadBalancersResult]): Future[AttachLoadBalancersResult] = ???

  override def attachLoadBalancersAsync(): Future[AttachLoadBalancersResult] = ???

  override def attachLoadBalancersAsync(asyncHandler: AsyncHandler[AttachLoadBalancersRequest, AttachLoadBalancersResult]): Future[AttachLoadBalancersResult] = ???

  override def batchDeleteScheduledActionAsync(batchDeleteScheduledActionRequest: BatchDeleteScheduledActionRequest): Future[BatchDeleteScheduledActionResult] = ???

  override def batchDeleteScheduledActionAsync(batchDeleteScheduledActionRequest: BatchDeleteScheduledActionRequest, asyncHandler: AsyncHandler[BatchDeleteScheduledActionRequest, BatchDeleteScheduledActionResult]): Future[BatchDeleteScheduledActionResult] = ???

  override def batchPutScheduledUpdateGroupActionAsync(batchPutScheduledUpdateGroupActionRequest: BatchPutScheduledUpdateGroupActionRequest): Future[BatchPutScheduledUpdateGroupActionResult] = ???

  override def batchPutScheduledUpdateGroupActionAsync(batchPutScheduledUpdateGroupActionRequest: BatchPutScheduledUpdateGroupActionRequest, asyncHandler: AsyncHandler[BatchPutScheduledUpdateGroupActionRequest, BatchPutScheduledUpdateGroupActionResult]): Future[BatchPutScheduledUpdateGroupActionResult] = ???

  override def completeLifecycleActionAsync(completeLifecycleActionRequest: CompleteLifecycleActionRequest): Future[CompleteLifecycleActionResult] = ???

  override def completeLifecycleActionAsync(completeLifecycleActionRequest: CompleteLifecycleActionRequest, asyncHandler: AsyncHandler[CompleteLifecycleActionRequest, CompleteLifecycleActionResult]): Future[CompleteLifecycleActionResult] = ???

  override def createAutoScalingGroupAsync(createAutoScalingGroupRequest: CreateAutoScalingGroupRequest): Future[CreateAutoScalingGroupResult] = ???

  override def createAutoScalingGroupAsync(createAutoScalingGroupRequest: CreateAutoScalingGroupRequest, asyncHandler: AsyncHandler[CreateAutoScalingGroupRequest, CreateAutoScalingGroupResult]): Future[CreateAutoScalingGroupResult] = ???

  override def createLaunchConfigurationAsync(createLaunchConfigurationRequest: CreateLaunchConfigurationRequest): Future[CreateLaunchConfigurationResult] = ???

  override def createLaunchConfigurationAsync(createLaunchConfigurationRequest: CreateLaunchConfigurationRequest, asyncHandler: AsyncHandler[CreateLaunchConfigurationRequest, CreateLaunchConfigurationResult]): Future[CreateLaunchConfigurationResult] = ???

  override def createOrUpdateTagsAsync(createOrUpdateTagsRequest: CreateOrUpdateTagsRequest): Future[CreateOrUpdateTagsResult] = ???

  override def createOrUpdateTagsAsync(createOrUpdateTagsRequest: CreateOrUpdateTagsRequest, asyncHandler: AsyncHandler[CreateOrUpdateTagsRequest, CreateOrUpdateTagsResult]): Future[CreateOrUpdateTagsResult] = ???

  override def deleteAutoScalingGroupAsync(deleteAutoScalingGroupRequest: DeleteAutoScalingGroupRequest): Future[DeleteAutoScalingGroupResult] = ???

  override def deleteAutoScalingGroupAsync(deleteAutoScalingGroupRequest: DeleteAutoScalingGroupRequest, asyncHandler: AsyncHandler[DeleteAutoScalingGroupRequest, DeleteAutoScalingGroupResult]): Future[DeleteAutoScalingGroupResult] = ???

  override def deleteLaunchConfigurationAsync(deleteLaunchConfigurationRequest: DeleteLaunchConfigurationRequest): Future[DeleteLaunchConfigurationResult] = ???

  override def deleteLaunchConfigurationAsync(deleteLaunchConfigurationRequest: DeleteLaunchConfigurationRequest, asyncHandler: AsyncHandler[DeleteLaunchConfigurationRequest, DeleteLaunchConfigurationResult]): Future[DeleteLaunchConfigurationResult] = ???

  override def deleteLifecycleHookAsync(deleteLifecycleHookRequest: DeleteLifecycleHookRequest): Future[DeleteLifecycleHookResult] = ???

  override def deleteLifecycleHookAsync(deleteLifecycleHookRequest: DeleteLifecycleHookRequest, asyncHandler: AsyncHandler[DeleteLifecycleHookRequest, DeleteLifecycleHookResult]): Future[DeleteLifecycleHookResult] = ???

  override def deleteNotificationConfigurationAsync(deleteNotificationConfigurationRequest: DeleteNotificationConfigurationRequest): Future[DeleteNotificationConfigurationResult] = ???

  override def deleteNotificationConfigurationAsync(deleteNotificationConfigurationRequest: DeleteNotificationConfigurationRequest, asyncHandler: AsyncHandler[DeleteNotificationConfigurationRequest, DeleteNotificationConfigurationResult]): Future[DeleteNotificationConfigurationResult] = ???

  override def deletePolicyAsync(deletePolicyRequest: DeletePolicyRequest): Future[DeletePolicyResult] = ???

  override def deletePolicyAsync(deletePolicyRequest: DeletePolicyRequest, asyncHandler: AsyncHandler[DeletePolicyRequest, DeletePolicyResult]): Future[DeletePolicyResult] = ???

  override def deleteScheduledActionAsync(deleteScheduledActionRequest: DeleteScheduledActionRequest): Future[DeleteScheduledActionResult] = ???

  override def deleteScheduledActionAsync(deleteScheduledActionRequest: DeleteScheduledActionRequest, asyncHandler: AsyncHandler[DeleteScheduledActionRequest, DeleteScheduledActionResult]): Future[DeleteScheduledActionResult] = ???

  override def deleteTagsAsync(deleteTagsRequest: DeleteTagsRequest): Future[DeleteTagsResult] = ???

  override def deleteTagsAsync(deleteTagsRequest: DeleteTagsRequest, asyncHandler: AsyncHandler[DeleteTagsRequest, DeleteTagsResult]): Future[DeleteTagsResult] = ???

  override def describeAccountLimitsAsync(describeAccountLimitsRequest: DescribeAccountLimitsRequest): Future[DescribeAccountLimitsResult] = ???

  override def describeAccountLimitsAsync(describeAccountLimitsRequest: DescribeAccountLimitsRequest, asyncHandler: AsyncHandler[DescribeAccountLimitsRequest, DescribeAccountLimitsResult]): Future[DescribeAccountLimitsResult] = ???

  override def describeAccountLimitsAsync(): Future[DescribeAccountLimitsResult] = ???

  override def describeAccountLimitsAsync(asyncHandler: AsyncHandler[DescribeAccountLimitsRequest, DescribeAccountLimitsResult]): Future[DescribeAccountLimitsResult] = ???

  override def describeAdjustmentTypesAsync(describeAdjustmentTypesRequest: DescribeAdjustmentTypesRequest): Future[DescribeAdjustmentTypesResult] = ???

  override def describeAdjustmentTypesAsync(describeAdjustmentTypesRequest: DescribeAdjustmentTypesRequest, asyncHandler: AsyncHandler[DescribeAdjustmentTypesRequest, DescribeAdjustmentTypesResult]): Future[DescribeAdjustmentTypesResult] = ???

  override def describeAdjustmentTypesAsync(): Future[DescribeAdjustmentTypesResult] = ???

  override def describeAdjustmentTypesAsync(asyncHandler: AsyncHandler[DescribeAdjustmentTypesRequest, DescribeAdjustmentTypesResult]): Future[DescribeAdjustmentTypesResult] = ???

  override def describeAutoScalingGroupsAsync(describeAutoScalingGroupsRequest: DescribeAutoScalingGroupsRequest): Future[DescribeAutoScalingGroupsResult] = ???

  override def describeAutoScalingGroupsAsync(describeAutoScalingGroupsRequest: DescribeAutoScalingGroupsRequest, asyncHandler: AsyncHandler[DescribeAutoScalingGroupsRequest, DescribeAutoScalingGroupsResult]): Future[DescribeAutoScalingGroupsResult] = ???

  override def describeAutoScalingGroupsAsync(): Future[DescribeAutoScalingGroupsResult] = ???

  override def describeAutoScalingGroupsAsync(asyncHandler: AsyncHandler[DescribeAutoScalingGroupsRequest, DescribeAutoScalingGroupsResult]): Future[DescribeAutoScalingGroupsResult] = ???

  override def describeAutoScalingInstancesAsync(describeAutoScalingInstancesRequest: DescribeAutoScalingInstancesRequest): Future[DescribeAutoScalingInstancesResult] = ???

  override def describeAutoScalingInstancesAsync(describeAutoScalingInstancesRequest: DescribeAutoScalingInstancesRequest, asyncHandler: AsyncHandler[DescribeAutoScalingInstancesRequest, DescribeAutoScalingInstancesResult]): Future[DescribeAutoScalingInstancesResult] = ???

  override def describeAutoScalingInstancesAsync(): Future[DescribeAutoScalingInstancesResult] = ???

  override def describeAutoScalingInstancesAsync(asyncHandler: AsyncHandler[DescribeAutoScalingInstancesRequest, DescribeAutoScalingInstancesResult]): Future[DescribeAutoScalingInstancesResult] = ???

  override def describeAutoScalingNotificationTypesAsync(describeAutoScalingNotificationTypesRequest: DescribeAutoScalingNotificationTypesRequest): Future[DescribeAutoScalingNotificationTypesResult] = ???

  override def describeAutoScalingNotificationTypesAsync(describeAutoScalingNotificationTypesRequest: DescribeAutoScalingNotificationTypesRequest, asyncHandler: AsyncHandler[DescribeAutoScalingNotificationTypesRequest, DescribeAutoScalingNotificationTypesResult]): Future[DescribeAutoScalingNotificationTypesResult] = ???

  override def describeAutoScalingNotificationTypesAsync(): Future[DescribeAutoScalingNotificationTypesResult] = ???

  override def describeAutoScalingNotificationTypesAsync(asyncHandler: AsyncHandler[DescribeAutoScalingNotificationTypesRequest, DescribeAutoScalingNotificationTypesResult]): Future[DescribeAutoScalingNotificationTypesResult] = ???

  override def describeLaunchConfigurationsAsync(describeLaunchConfigurationsRequest: DescribeLaunchConfigurationsRequest): Future[DescribeLaunchConfigurationsResult] = ???

  override def describeLaunchConfigurationsAsync(describeLaunchConfigurationsRequest: DescribeLaunchConfigurationsRequest, asyncHandler: AsyncHandler[DescribeLaunchConfigurationsRequest, DescribeLaunchConfigurationsResult]): Future[DescribeLaunchConfigurationsResult] = ???

  override def describeLaunchConfigurationsAsync(): Future[DescribeLaunchConfigurationsResult] = ???

  override def describeLaunchConfigurationsAsync(asyncHandler: AsyncHandler[DescribeLaunchConfigurationsRequest, DescribeLaunchConfigurationsResult]): Future[DescribeLaunchConfigurationsResult] = ???

  override def describeLifecycleHookTypesAsync(describeLifecycleHookTypesRequest: DescribeLifecycleHookTypesRequest): Future[DescribeLifecycleHookTypesResult] = ???

  override def describeLifecycleHookTypesAsync(describeLifecycleHookTypesRequest: DescribeLifecycleHookTypesRequest, asyncHandler: AsyncHandler[DescribeLifecycleHookTypesRequest, DescribeLifecycleHookTypesResult]): Future[DescribeLifecycleHookTypesResult] = ???

  override def describeLifecycleHookTypesAsync(): Future[DescribeLifecycleHookTypesResult] = ???

  override def describeLifecycleHookTypesAsync(asyncHandler: AsyncHandler[DescribeLifecycleHookTypesRequest, DescribeLifecycleHookTypesResult]): Future[DescribeLifecycleHookTypesResult] = ???

  override def describeLifecycleHooksAsync(describeLifecycleHooksRequest: DescribeLifecycleHooksRequest): Future[DescribeLifecycleHooksResult] = ???

  override def describeLifecycleHooksAsync(describeLifecycleHooksRequest: DescribeLifecycleHooksRequest, asyncHandler: AsyncHandler[DescribeLifecycleHooksRequest, DescribeLifecycleHooksResult]): Future[DescribeLifecycleHooksResult] = ???

  override def describeLoadBalancerTargetGroupsAsync(describeLoadBalancerTargetGroupsRequest: DescribeLoadBalancerTargetGroupsRequest): Future[DescribeLoadBalancerTargetGroupsResult] = ???

  override def describeLoadBalancerTargetGroupsAsync(describeLoadBalancerTargetGroupsRequest: DescribeLoadBalancerTargetGroupsRequest, asyncHandler: AsyncHandler[DescribeLoadBalancerTargetGroupsRequest, DescribeLoadBalancerTargetGroupsResult]): Future[DescribeLoadBalancerTargetGroupsResult] = ???

  override def describeLoadBalancersAsync(describeLoadBalancersRequest: DescribeLoadBalancersRequest): Future[DescribeLoadBalancersResult] = ???

  override def describeLoadBalancersAsync(describeLoadBalancersRequest: DescribeLoadBalancersRequest, asyncHandler: AsyncHandler[DescribeLoadBalancersRequest, DescribeLoadBalancersResult]): Future[DescribeLoadBalancersResult] = ???

  override def describeMetricCollectionTypesAsync(describeMetricCollectionTypesRequest: DescribeMetricCollectionTypesRequest): Future[DescribeMetricCollectionTypesResult] = ???

  override def describeMetricCollectionTypesAsync(describeMetricCollectionTypesRequest: DescribeMetricCollectionTypesRequest, asyncHandler: AsyncHandler[DescribeMetricCollectionTypesRequest, DescribeMetricCollectionTypesResult]): Future[DescribeMetricCollectionTypesResult] = ???

  override def describeMetricCollectionTypesAsync(): Future[DescribeMetricCollectionTypesResult] = ???

  override def describeMetricCollectionTypesAsync(asyncHandler: AsyncHandler[DescribeMetricCollectionTypesRequest, DescribeMetricCollectionTypesResult]): Future[DescribeMetricCollectionTypesResult] = ???

  override def describeNotificationConfigurationsAsync(describeNotificationConfigurationsRequest: DescribeNotificationConfigurationsRequest): Future[DescribeNotificationConfigurationsResult] = ???

  override def describeNotificationConfigurationsAsync(describeNotificationConfigurationsRequest: DescribeNotificationConfigurationsRequest, asyncHandler: AsyncHandler[DescribeNotificationConfigurationsRequest, DescribeNotificationConfigurationsResult]): Future[DescribeNotificationConfigurationsResult] = ???

  override def describeNotificationConfigurationsAsync(): Future[DescribeNotificationConfigurationsResult] = ???

  override def describeNotificationConfigurationsAsync(asyncHandler: AsyncHandler[DescribeNotificationConfigurationsRequest, DescribeNotificationConfigurationsResult]): Future[DescribeNotificationConfigurationsResult] = ???

  override def describePoliciesAsync(describePoliciesRequest: DescribePoliciesRequest): Future[DescribePoliciesResult] = ???

  override def describePoliciesAsync(describePoliciesRequest: DescribePoliciesRequest, asyncHandler: AsyncHandler[DescribePoliciesRequest, DescribePoliciesResult]): Future[DescribePoliciesResult] = ???

  override def describePoliciesAsync(): Future[DescribePoliciesResult] = ???

  override def describePoliciesAsync(asyncHandler: AsyncHandler[DescribePoliciesRequest, DescribePoliciesResult]): Future[DescribePoliciesResult] = ???

  override def describeScalingActivitiesAsync(describeScalingActivitiesRequest: DescribeScalingActivitiesRequest): Future[DescribeScalingActivitiesResult] = ???

  override def describeScalingActivitiesAsync(describeScalingActivitiesRequest: DescribeScalingActivitiesRequest, asyncHandler: AsyncHandler[DescribeScalingActivitiesRequest, DescribeScalingActivitiesResult]): Future[DescribeScalingActivitiesResult] = ???

  override def describeScalingActivitiesAsync(): Future[DescribeScalingActivitiesResult] = ???

  override def describeScalingActivitiesAsync(asyncHandler: AsyncHandler[DescribeScalingActivitiesRequest, DescribeScalingActivitiesResult]): Future[DescribeScalingActivitiesResult] = ???

  override def describeScalingProcessTypesAsync(describeScalingProcessTypesRequest: DescribeScalingProcessTypesRequest): Future[DescribeScalingProcessTypesResult] = ???

  override def describeScalingProcessTypesAsync(describeScalingProcessTypesRequest: DescribeScalingProcessTypesRequest, asyncHandler: AsyncHandler[DescribeScalingProcessTypesRequest, DescribeScalingProcessTypesResult]): Future[DescribeScalingProcessTypesResult] = ???

  override def describeScalingProcessTypesAsync(): Future[DescribeScalingProcessTypesResult] = ???

  override def describeScalingProcessTypesAsync(asyncHandler: AsyncHandler[DescribeScalingProcessTypesRequest, DescribeScalingProcessTypesResult]): Future[DescribeScalingProcessTypesResult] = ???

  override def describeScheduledActionsAsync(describeScheduledActionsRequest: DescribeScheduledActionsRequest): Future[DescribeScheduledActionsResult] = ???

  override def describeScheduledActionsAsync(describeScheduledActionsRequest: DescribeScheduledActionsRequest, asyncHandler: AsyncHandler[DescribeScheduledActionsRequest, DescribeScheduledActionsResult]): Future[DescribeScheduledActionsResult] = ???

  override def describeScheduledActionsAsync(): Future[DescribeScheduledActionsResult] = ???

  override def describeScheduledActionsAsync(asyncHandler: AsyncHandler[DescribeScheduledActionsRequest, DescribeScheduledActionsResult]): Future[DescribeScheduledActionsResult] = ???

  override def describeTagsAsync(describeTagsRequest: DescribeTagsRequest): Future[DescribeTagsResult] = ???

  override def describeTagsAsync(describeTagsRequest: DescribeTagsRequest, asyncHandler: AsyncHandler[DescribeTagsRequest, DescribeTagsResult]): Future[DescribeTagsResult] = ???

  override def describeTagsAsync(): Future[DescribeTagsResult] = ???

  override def describeTagsAsync(asyncHandler: AsyncHandler[DescribeTagsRequest, DescribeTagsResult]): Future[DescribeTagsResult] = ???

  override def describeTerminationPolicyTypesAsync(describeTerminationPolicyTypesRequest: DescribeTerminationPolicyTypesRequest): Future[DescribeTerminationPolicyTypesResult] = ???

  override def describeTerminationPolicyTypesAsync(describeTerminationPolicyTypesRequest: DescribeTerminationPolicyTypesRequest, asyncHandler: AsyncHandler[DescribeTerminationPolicyTypesRequest, DescribeTerminationPolicyTypesResult]): Future[DescribeTerminationPolicyTypesResult] = ???

  override def describeTerminationPolicyTypesAsync(): Future[DescribeTerminationPolicyTypesResult] = ???

  override def describeTerminationPolicyTypesAsync(asyncHandler: AsyncHandler[DescribeTerminationPolicyTypesRequest, DescribeTerminationPolicyTypesResult]): Future[DescribeTerminationPolicyTypesResult] = ???

  override def detachInstancesAsync(detachInstancesRequest: DetachInstancesRequest): Future[DetachInstancesResult] = ???

  override def detachInstancesAsync(detachInstancesRequest: DetachInstancesRequest, asyncHandler: AsyncHandler[DetachInstancesRequest, DetachInstancesResult]): Future[DetachInstancesResult] = ???

  override def detachLoadBalancerTargetGroupsAsync(detachLoadBalancerTargetGroupsRequest: DetachLoadBalancerTargetGroupsRequest): Future[DetachLoadBalancerTargetGroupsResult] = ???

  override def detachLoadBalancerTargetGroupsAsync(detachLoadBalancerTargetGroupsRequest: DetachLoadBalancerTargetGroupsRequest, asyncHandler: AsyncHandler[DetachLoadBalancerTargetGroupsRequest, DetachLoadBalancerTargetGroupsResult]): Future[DetachLoadBalancerTargetGroupsResult] = ???

  override def detachLoadBalancersAsync(detachLoadBalancersRequest: DetachLoadBalancersRequest): Future[DetachLoadBalancersResult] = ???

  override def detachLoadBalancersAsync(detachLoadBalancersRequest: DetachLoadBalancersRequest, asyncHandler: AsyncHandler[DetachLoadBalancersRequest, DetachLoadBalancersResult]): Future[DetachLoadBalancersResult] = ???

  override def detachLoadBalancersAsync(): Future[DetachLoadBalancersResult] = ???

  override def detachLoadBalancersAsync(asyncHandler: AsyncHandler[DetachLoadBalancersRequest, DetachLoadBalancersResult]): Future[DetachLoadBalancersResult] = ???

  override def disableMetricsCollectionAsync(disableMetricsCollectionRequest: DisableMetricsCollectionRequest): Future[DisableMetricsCollectionResult] = ???

  override def disableMetricsCollectionAsync(disableMetricsCollectionRequest: DisableMetricsCollectionRequest, asyncHandler: AsyncHandler[DisableMetricsCollectionRequest, DisableMetricsCollectionResult]): Future[DisableMetricsCollectionResult] = ???

  override def enableMetricsCollectionAsync(enableMetricsCollectionRequest: EnableMetricsCollectionRequest): Future[EnableMetricsCollectionResult] = ???

  override def enableMetricsCollectionAsync(enableMetricsCollectionRequest: EnableMetricsCollectionRequest, asyncHandler: AsyncHandler[EnableMetricsCollectionRequest, EnableMetricsCollectionResult]): Future[EnableMetricsCollectionResult] = ???

  override def enterStandbyAsync(enterStandbyRequest: EnterStandbyRequest): Future[EnterStandbyResult] = ???

  override def enterStandbyAsync(enterStandbyRequest: EnterStandbyRequest, asyncHandler: AsyncHandler[EnterStandbyRequest, EnterStandbyResult]): Future[EnterStandbyResult] = ???

  override def executePolicyAsync(executePolicyRequest: ExecutePolicyRequest): Future[ExecutePolicyResult] = ???

  override def executePolicyAsync(executePolicyRequest: ExecutePolicyRequest, asyncHandler: AsyncHandler[ExecutePolicyRequest, ExecutePolicyResult]): Future[ExecutePolicyResult] = ???

  override def exitStandbyAsync(exitStandbyRequest: ExitStandbyRequest): Future[ExitStandbyResult] = ???

  override def exitStandbyAsync(exitStandbyRequest: ExitStandbyRequest, asyncHandler: AsyncHandler[ExitStandbyRequest, ExitStandbyResult]): Future[ExitStandbyResult] = ???

  override def putLifecycleHookAsync(putLifecycleHookRequest: PutLifecycleHookRequest): Future[PutLifecycleHookResult] = ???

  override def putLifecycleHookAsync(putLifecycleHookRequest: PutLifecycleHookRequest, asyncHandler: AsyncHandler[PutLifecycleHookRequest, PutLifecycleHookResult]): Future[PutLifecycleHookResult] = ???

  override def putNotificationConfigurationAsync(putNotificationConfigurationRequest: PutNotificationConfigurationRequest): Future[PutNotificationConfigurationResult] = ???

  override def putNotificationConfigurationAsync(putNotificationConfigurationRequest: PutNotificationConfigurationRequest, asyncHandler: AsyncHandler[PutNotificationConfigurationRequest, PutNotificationConfigurationResult]): Future[PutNotificationConfigurationResult] = ???

  override def putScalingPolicyAsync(putScalingPolicyRequest: PutScalingPolicyRequest): Future[PutScalingPolicyResult] = ???

  override def putScalingPolicyAsync(putScalingPolicyRequest: PutScalingPolicyRequest, asyncHandler: AsyncHandler[PutScalingPolicyRequest, PutScalingPolicyResult]): Future[PutScalingPolicyResult] = ???

  override def putScheduledUpdateGroupActionAsync(putScheduledUpdateGroupActionRequest: PutScheduledUpdateGroupActionRequest): Future[PutScheduledUpdateGroupActionResult] = ???

  override def putScheduledUpdateGroupActionAsync(putScheduledUpdateGroupActionRequest: PutScheduledUpdateGroupActionRequest, asyncHandler: AsyncHandler[PutScheduledUpdateGroupActionRequest, PutScheduledUpdateGroupActionResult]): Future[PutScheduledUpdateGroupActionResult] = ???

  override def recordLifecycleActionHeartbeatAsync(recordLifecycleActionHeartbeatRequest: RecordLifecycleActionHeartbeatRequest): Future[RecordLifecycleActionHeartbeatResult] = ???

  override def recordLifecycleActionHeartbeatAsync(recordLifecycleActionHeartbeatRequest: RecordLifecycleActionHeartbeatRequest, asyncHandler: AsyncHandler[RecordLifecycleActionHeartbeatRequest, RecordLifecycleActionHeartbeatResult]): Future[RecordLifecycleActionHeartbeatResult] = ???

  override def resumeProcessesAsync(resumeProcessesRequest: ResumeProcessesRequest): Future[ResumeProcessesResult] = ???

  override def resumeProcessesAsync(resumeProcessesRequest: ResumeProcessesRequest, asyncHandler: AsyncHandler[ResumeProcessesRequest, ResumeProcessesResult]): Future[ResumeProcessesResult] = ???

  override def setDesiredCapacityAsync(setDesiredCapacityRequest: SetDesiredCapacityRequest): Future[SetDesiredCapacityResult] = ???

  override def setDesiredCapacityAsync(setDesiredCapacityRequest: SetDesiredCapacityRequest, asyncHandler: AsyncHandler[SetDesiredCapacityRequest, SetDesiredCapacityResult]): Future[SetDesiredCapacityResult] = ???

  override def setInstanceHealthAsync(setInstanceHealthRequest: SetInstanceHealthRequest): Future[SetInstanceHealthResult] = ???

  override def setInstanceHealthAsync(setInstanceHealthRequest: SetInstanceHealthRequest, asyncHandler: AsyncHandler[SetInstanceHealthRequest, SetInstanceHealthResult]): Future[SetInstanceHealthResult] = ???

  override def setInstanceProtectionAsync(setInstanceProtectionRequest: SetInstanceProtectionRequest): Future[SetInstanceProtectionResult] = ???

  override def setInstanceProtectionAsync(setInstanceProtectionRequest: SetInstanceProtectionRequest, asyncHandler: AsyncHandler[SetInstanceProtectionRequest, SetInstanceProtectionResult]): Future[SetInstanceProtectionResult] = ???

  override def suspendProcessesAsync(suspendProcessesRequest: SuspendProcessesRequest): Future[SuspendProcessesResult] = ???

  override def suspendProcessesAsync(suspendProcessesRequest: SuspendProcessesRequest, asyncHandler: AsyncHandler[SuspendProcessesRequest, SuspendProcessesResult]): Future[SuspendProcessesResult] = ???

  override def terminateInstanceInAutoScalingGroupAsync(terminateInstanceInAutoScalingGroupRequest: TerminateInstanceInAutoScalingGroupRequest): Future[TerminateInstanceInAutoScalingGroupResult] = ???

  override def terminateInstanceInAutoScalingGroupAsync(terminateInstanceInAutoScalingGroupRequest: TerminateInstanceInAutoScalingGroupRequest, asyncHandler: AsyncHandler[TerminateInstanceInAutoScalingGroupRequest, TerminateInstanceInAutoScalingGroupResult]): Future[TerminateInstanceInAutoScalingGroupResult] = ???

  override def updateAutoScalingGroupAsync(updateAutoScalingGroupRequest: UpdateAutoScalingGroupRequest): Future[UpdateAutoScalingGroupResult] = ???

  override def updateAutoScalingGroupAsync(updateAutoScalingGroupRequest: UpdateAutoScalingGroupRequest, asyncHandler: AsyncHandler[UpdateAutoScalingGroupRequest, UpdateAutoScalingGroupResult]): Future[UpdateAutoScalingGroupResult] = ???

  override def setEndpoint(endpoint: String): Unit = ???

  override def setRegion(region: Region): Unit = ???

  override def attachInstances(attachInstancesRequest: AttachInstancesRequest): AttachInstancesResult = ???

  override def attachLoadBalancerTargetGroups(attachLoadBalancerTargetGroupsRequest: AttachLoadBalancerTargetGroupsRequest): AttachLoadBalancerTargetGroupsResult = ???

  override def attachLoadBalancers(attachLoadBalancersRequest: AttachLoadBalancersRequest): AttachLoadBalancersResult = ???

  override def attachLoadBalancers(): AttachLoadBalancersResult = ???

  override def batchDeleteScheduledAction(batchDeleteScheduledActionRequest: BatchDeleteScheduledActionRequest): BatchDeleteScheduledActionResult = ???

  override def batchPutScheduledUpdateGroupAction(batchPutScheduledUpdateGroupActionRequest: BatchPutScheduledUpdateGroupActionRequest): BatchPutScheduledUpdateGroupActionResult = ???

  override def completeLifecycleAction(completeLifecycleActionRequest: CompleteLifecycleActionRequest): CompleteLifecycleActionResult = ???

  override def createAutoScalingGroup(createAutoScalingGroupRequest: CreateAutoScalingGroupRequest): CreateAutoScalingGroupResult = ???

  override def createLaunchConfiguration(createLaunchConfigurationRequest: CreateLaunchConfigurationRequest): CreateLaunchConfigurationResult = ???

  override def createOrUpdateTags(createOrUpdateTagsRequest: CreateOrUpdateTagsRequest): CreateOrUpdateTagsResult = ???

  override def deleteAutoScalingGroup(deleteAutoScalingGroupRequest: DeleteAutoScalingGroupRequest): DeleteAutoScalingGroupResult = ???

  override def deleteLaunchConfiguration(deleteLaunchConfigurationRequest: DeleteLaunchConfigurationRequest): DeleteLaunchConfigurationResult = ???

  override def deleteLifecycleHook(deleteLifecycleHookRequest: DeleteLifecycleHookRequest): DeleteLifecycleHookResult = ???

  override def deleteNotificationConfiguration(deleteNotificationConfigurationRequest: DeleteNotificationConfigurationRequest): DeleteNotificationConfigurationResult = ???

  override def deletePolicy(deletePolicyRequest: DeletePolicyRequest): DeletePolicyResult = ???

  override def deleteScheduledAction(deleteScheduledActionRequest: DeleteScheduledActionRequest): DeleteScheduledActionResult = ???

  override def deleteTags(deleteTagsRequest: DeleteTagsRequest): DeleteTagsResult = ???

  override def describeAccountLimits(describeAccountLimitsRequest: DescribeAccountLimitsRequest): DescribeAccountLimitsResult = ???

  override def describeAccountLimits(): DescribeAccountLimitsResult = ???

  override def describeAdjustmentTypes(describeAdjustmentTypesRequest: DescribeAdjustmentTypesRequest): DescribeAdjustmentTypesResult = ???

  override def describeAdjustmentTypes(): DescribeAdjustmentTypesResult = ???

  override def describeAutoScalingGroups(describeAutoScalingGroupsRequest: DescribeAutoScalingGroupsRequest): DescribeAutoScalingGroupsResult = ???

  override def describeAutoScalingGroups(): DescribeAutoScalingGroupsResult = ???

  override def describeAutoScalingInstances(describeAutoScalingInstancesRequest: DescribeAutoScalingInstancesRequest): DescribeAutoScalingInstancesResult = ???

  override def describeAutoScalingInstances(): DescribeAutoScalingInstancesResult = ???

  override def describeAutoScalingNotificationTypes(describeAutoScalingNotificationTypesRequest: DescribeAutoScalingNotificationTypesRequest): DescribeAutoScalingNotificationTypesResult = ???

  override def describeAutoScalingNotificationTypes(): DescribeAutoScalingNotificationTypesResult = ???

  override def describeLaunchConfigurations(describeLaunchConfigurationsRequest: DescribeLaunchConfigurationsRequest): DescribeLaunchConfigurationsResult = ???

  override def describeLaunchConfigurations(): DescribeLaunchConfigurationsResult = ???

  override def describeLifecycleHookTypes(describeLifecycleHookTypesRequest: DescribeLifecycleHookTypesRequest): DescribeLifecycleHookTypesResult = ???

  override def describeLifecycleHookTypes(): DescribeLifecycleHookTypesResult = ???

  override def describeLifecycleHooks(describeLifecycleHooksRequest: DescribeLifecycleHooksRequest): DescribeLifecycleHooksResult = ???

  override def describeLoadBalancerTargetGroups(describeLoadBalancerTargetGroupsRequest: DescribeLoadBalancerTargetGroupsRequest): DescribeLoadBalancerTargetGroupsResult = ???

  override def describeLoadBalancers(describeLoadBalancersRequest: DescribeLoadBalancersRequest): DescribeLoadBalancersResult = ???

  override def describeMetricCollectionTypes(describeMetricCollectionTypesRequest: DescribeMetricCollectionTypesRequest): DescribeMetricCollectionTypesResult = ???

  override def describeMetricCollectionTypes(): DescribeMetricCollectionTypesResult = ???

  override def describeNotificationConfigurations(describeNotificationConfigurationsRequest: DescribeNotificationConfigurationsRequest): DescribeNotificationConfigurationsResult = ???

  override def describeNotificationConfigurations(): DescribeNotificationConfigurationsResult = ???

  override def describePolicies(describePoliciesRequest: DescribePoliciesRequest): DescribePoliciesResult = ???

  override def describePolicies(): DescribePoliciesResult = ???

  override def describeScalingActivities(describeScalingActivitiesRequest: DescribeScalingActivitiesRequest): DescribeScalingActivitiesResult = ???

  override def describeScalingActivities(): DescribeScalingActivitiesResult = ???

  override def describeScalingProcessTypes(describeScalingProcessTypesRequest: DescribeScalingProcessTypesRequest): DescribeScalingProcessTypesResult = ???

  override def describeScalingProcessTypes(): DescribeScalingProcessTypesResult = ???

  override def describeScheduledActions(describeScheduledActionsRequest: DescribeScheduledActionsRequest): DescribeScheduledActionsResult = ???

  override def describeScheduledActions(): DescribeScheduledActionsResult = ???

  override def describeTags(describeTagsRequest: DescribeTagsRequest): DescribeTagsResult = ???

  override def describeTags(): DescribeTagsResult = ???

  override def describeTerminationPolicyTypes(describeTerminationPolicyTypesRequest: DescribeTerminationPolicyTypesRequest): DescribeTerminationPolicyTypesResult = ???

  override def describeTerminationPolicyTypes(): DescribeTerminationPolicyTypesResult = ???

  override def detachInstances(detachInstancesRequest: DetachInstancesRequest): DetachInstancesResult = ???

  override def detachLoadBalancerTargetGroups(detachLoadBalancerTargetGroupsRequest: DetachLoadBalancerTargetGroupsRequest): DetachLoadBalancerTargetGroupsResult = ???

  override def detachLoadBalancers(detachLoadBalancersRequest: DetachLoadBalancersRequest): DetachLoadBalancersResult = ???

  override def detachLoadBalancers(): DetachLoadBalancersResult = ???

  override def disableMetricsCollection(disableMetricsCollectionRequest: DisableMetricsCollectionRequest): DisableMetricsCollectionResult = ???

  override def enableMetricsCollection(enableMetricsCollectionRequest: EnableMetricsCollectionRequest): EnableMetricsCollectionResult = ???

  override def enterStandby(enterStandbyRequest: EnterStandbyRequest): EnterStandbyResult = ???

  override def executePolicy(executePolicyRequest: ExecutePolicyRequest): ExecutePolicyResult = ???

  override def exitStandby(exitStandbyRequest: ExitStandbyRequest): ExitStandbyResult = ???

  override def putLifecycleHook(putLifecycleHookRequest: PutLifecycleHookRequest): PutLifecycleHookResult = ???

  override def putNotificationConfiguration(putNotificationConfigurationRequest: PutNotificationConfigurationRequest): PutNotificationConfigurationResult = ???

  override def putScalingPolicy(putScalingPolicyRequest: PutScalingPolicyRequest): PutScalingPolicyResult = ???

  override def putScheduledUpdateGroupAction(putScheduledUpdateGroupActionRequest: PutScheduledUpdateGroupActionRequest): PutScheduledUpdateGroupActionResult = ???

  override def recordLifecycleActionHeartbeat(recordLifecycleActionHeartbeatRequest: RecordLifecycleActionHeartbeatRequest): RecordLifecycleActionHeartbeatResult = ???

  override def resumeProcesses(resumeProcessesRequest: ResumeProcessesRequest): ResumeProcessesResult = ???

  override def setDesiredCapacity(setDesiredCapacityRequest: SetDesiredCapacityRequest): SetDesiredCapacityResult = ???

  override def setInstanceHealth(setInstanceHealthRequest: SetInstanceHealthRequest): SetInstanceHealthResult = ???

  override def setInstanceProtection(setInstanceProtectionRequest: SetInstanceProtectionRequest): SetInstanceProtectionResult = ???

  override def suspendProcesses(suspendProcessesRequest: SuspendProcessesRequest): SuspendProcessesResult = ???

  override def terminateInstanceInAutoScalingGroup(terminateInstanceInAutoScalingGroupRequest: TerminateInstanceInAutoScalingGroupRequest): TerminateInstanceInAutoScalingGroupResult = ???

  override def updateAutoScalingGroup(updateAutoScalingGroupRequest: UpdateAutoScalingGroupRequest): UpdateAutoScalingGroupResult = ???

  override def shutdown(): Unit = ???

  override def getCachedResponseMetadata(request: AmazonWebServiceRequest): ResponseMetadata = ???

  override def waiters(): AmazonAutoScalingWaiters = ???
}
