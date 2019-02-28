package com.dwolla.aws.ecs

import java.util.concurrent.Future

import com.amazonaws.{AmazonWebServiceRequest, ResponseMetadata}
import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.regions.Region
import com.amazonaws.services.ecs.AmazonECSAsync
import com.amazonaws.services.ecs.model._
import com.amazonaws.services.ecs.waiters.AmazonECSWaiters

abstract class FakeECSAsync extends AmazonECSAsync {
  override def createClusterAsync(createClusterRequest: CreateClusterRequest): Future[CreateClusterResult] = ???

  override def createClusterAsync(createClusterRequest: CreateClusterRequest, asyncHandler: AsyncHandler[CreateClusterRequest, CreateClusterResult]): Future[CreateClusterResult] = ???

  override def createClusterAsync(): Future[CreateClusterResult] = ???

  override def createClusterAsync(asyncHandler: AsyncHandler[CreateClusterRequest, CreateClusterResult]): Future[CreateClusterResult] = ???

  override def createServiceAsync(createServiceRequest: CreateServiceRequest): Future[CreateServiceResult] = ???

  override def createServiceAsync(createServiceRequest: CreateServiceRequest, asyncHandler: AsyncHandler[CreateServiceRequest, CreateServiceResult]): Future[CreateServiceResult] = ???

  override def deleteAccountSettingAsync(deleteAccountSettingRequest: DeleteAccountSettingRequest): Future[DeleteAccountSettingResult] = ???

  override def deleteAccountSettingAsync(deleteAccountSettingRequest: DeleteAccountSettingRequest, asyncHandler: AsyncHandler[DeleteAccountSettingRequest, DeleteAccountSettingResult]): Future[DeleteAccountSettingResult] = ???

  override def deleteAttributesAsync(deleteAttributesRequest: DeleteAttributesRequest): Future[DeleteAttributesResult] = ???

  override def deleteAttributesAsync(deleteAttributesRequest: DeleteAttributesRequest, asyncHandler: AsyncHandler[DeleteAttributesRequest, DeleteAttributesResult]): Future[DeleteAttributesResult] = ???

  override def deleteClusterAsync(deleteClusterRequest: DeleteClusterRequest): Future[DeleteClusterResult] = ???

  override def deleteClusterAsync(deleteClusterRequest: DeleteClusterRequest, asyncHandler: AsyncHandler[DeleteClusterRequest, DeleteClusterResult]): Future[DeleteClusterResult] = ???

  override def deleteServiceAsync(deleteServiceRequest: DeleteServiceRequest): Future[DeleteServiceResult] = ???

  override def deleteServiceAsync(deleteServiceRequest: DeleteServiceRequest, asyncHandler: AsyncHandler[DeleteServiceRequest, DeleteServiceResult]): Future[DeleteServiceResult] = ???

  override def deregisterContainerInstanceAsync(deregisterContainerInstanceRequest: DeregisterContainerInstanceRequest): Future[DeregisterContainerInstanceResult] = ???

  override def deregisterContainerInstanceAsync(deregisterContainerInstanceRequest: DeregisterContainerInstanceRequest, asyncHandler: AsyncHandler[DeregisterContainerInstanceRequest, DeregisterContainerInstanceResult]): Future[DeregisterContainerInstanceResult] = ???

  override def deregisterTaskDefinitionAsync(deregisterTaskDefinitionRequest: DeregisterTaskDefinitionRequest): Future[DeregisterTaskDefinitionResult] = ???

  override def deregisterTaskDefinitionAsync(deregisterTaskDefinitionRequest: DeregisterTaskDefinitionRequest, asyncHandler: AsyncHandler[DeregisterTaskDefinitionRequest, DeregisterTaskDefinitionResult]): Future[DeregisterTaskDefinitionResult] = ???

  override def describeClustersAsync(describeClustersRequest: DescribeClustersRequest): Future[DescribeClustersResult] = ???

  override def describeClustersAsync(describeClustersRequest: DescribeClustersRequest, asyncHandler: AsyncHandler[DescribeClustersRequest, DescribeClustersResult]): Future[DescribeClustersResult] = ???

  override def describeClustersAsync(): Future[DescribeClustersResult] = ???

  override def describeClustersAsync(asyncHandler: AsyncHandler[DescribeClustersRequest, DescribeClustersResult]): Future[DescribeClustersResult] = ???

  override def describeContainerInstancesAsync(describeContainerInstancesRequest: DescribeContainerInstancesRequest): Future[DescribeContainerInstancesResult] = ???

  override def describeContainerInstancesAsync(describeContainerInstancesRequest: DescribeContainerInstancesRequest, asyncHandler: AsyncHandler[DescribeContainerInstancesRequest, DescribeContainerInstancesResult]): Future[DescribeContainerInstancesResult] = ???

  override def describeServicesAsync(describeServicesRequest: DescribeServicesRequest): Future[DescribeServicesResult] = ???

  override def describeServicesAsync(describeServicesRequest: DescribeServicesRequest, asyncHandler: AsyncHandler[DescribeServicesRequest, DescribeServicesResult]): Future[DescribeServicesResult] = ???

  override def describeTaskDefinitionAsync(describeTaskDefinitionRequest: DescribeTaskDefinitionRequest): Future[DescribeTaskDefinitionResult] = ???

  override def describeTaskDefinitionAsync(describeTaskDefinitionRequest: DescribeTaskDefinitionRequest, asyncHandler: AsyncHandler[DescribeTaskDefinitionRequest, DescribeTaskDefinitionResult]): Future[DescribeTaskDefinitionResult] = ???

  override def describeTasksAsync(describeTasksRequest: DescribeTasksRequest): Future[DescribeTasksResult] = ???

  override def describeTasksAsync(describeTasksRequest: DescribeTasksRequest, asyncHandler: AsyncHandler[DescribeTasksRequest, DescribeTasksResult]): Future[DescribeTasksResult] = ???

  override def discoverPollEndpointAsync(discoverPollEndpointRequest: DiscoverPollEndpointRequest): Future[DiscoverPollEndpointResult] = ???

  override def discoverPollEndpointAsync(discoverPollEndpointRequest: DiscoverPollEndpointRequest, asyncHandler: AsyncHandler[DiscoverPollEndpointRequest, DiscoverPollEndpointResult]): Future[DiscoverPollEndpointResult] = ???

  override def discoverPollEndpointAsync(): Future[DiscoverPollEndpointResult] = ???

  override def discoverPollEndpointAsync(asyncHandler: AsyncHandler[DiscoverPollEndpointRequest, DiscoverPollEndpointResult]): Future[DiscoverPollEndpointResult] = ???

  override def listAccountSettingsAsync(listAccountSettingsRequest: ListAccountSettingsRequest): Future[ListAccountSettingsResult] = ???

  override def listAccountSettingsAsync(listAccountSettingsRequest: ListAccountSettingsRequest, asyncHandler: AsyncHandler[ListAccountSettingsRequest, ListAccountSettingsResult]): Future[ListAccountSettingsResult] = ???

  override def listAttributesAsync(listAttributesRequest: ListAttributesRequest): Future[ListAttributesResult] = ???

  override def listAttributesAsync(listAttributesRequest: ListAttributesRequest, asyncHandler: AsyncHandler[ListAttributesRequest, ListAttributesResult]): Future[ListAttributesResult] = ???

  override def listClustersAsync(listClustersRequest: ListClustersRequest): Future[ListClustersResult] = ???

  override def listClustersAsync(listClustersRequest: ListClustersRequest, asyncHandler: AsyncHandler[ListClustersRequest, ListClustersResult]): Future[ListClustersResult] = ???

  override def listClustersAsync(): Future[ListClustersResult] = ???

  override def listClustersAsync(asyncHandler: AsyncHandler[ListClustersRequest, ListClustersResult]): Future[ListClustersResult] = ???

  override def listContainerInstancesAsync(listContainerInstancesRequest: ListContainerInstancesRequest): Future[ListContainerInstancesResult] = ???

  override def listContainerInstancesAsync(listContainerInstancesRequest: ListContainerInstancesRequest, asyncHandler: AsyncHandler[ListContainerInstancesRequest, ListContainerInstancesResult]): Future[ListContainerInstancesResult] = ???

  override def listContainerInstancesAsync(): Future[ListContainerInstancesResult] = ???

  override def listContainerInstancesAsync(asyncHandler: AsyncHandler[ListContainerInstancesRequest, ListContainerInstancesResult]): Future[ListContainerInstancesResult] = ???

  override def listServicesAsync(listServicesRequest: ListServicesRequest): Future[ListServicesResult] = ???

  override def listServicesAsync(listServicesRequest: ListServicesRequest, asyncHandler: AsyncHandler[ListServicesRequest, ListServicesResult]): Future[ListServicesResult] = ???

  override def listServicesAsync(): Future[ListServicesResult] = ???

  override def listServicesAsync(asyncHandler: AsyncHandler[ListServicesRequest, ListServicesResult]): Future[ListServicesResult] = ???

  override def listTagsForResourceAsync(listTagsForResourceRequest: ListTagsForResourceRequest): Future[ListTagsForResourceResult] = ???

  override def listTagsForResourceAsync(listTagsForResourceRequest: ListTagsForResourceRequest, asyncHandler: AsyncHandler[ListTagsForResourceRequest, ListTagsForResourceResult]): Future[ListTagsForResourceResult] = ???

  override def listTaskDefinitionFamiliesAsync(listTaskDefinitionFamiliesRequest: ListTaskDefinitionFamiliesRequest): Future[ListTaskDefinitionFamiliesResult] = ???

  override def listTaskDefinitionFamiliesAsync(listTaskDefinitionFamiliesRequest: ListTaskDefinitionFamiliesRequest, asyncHandler: AsyncHandler[ListTaskDefinitionFamiliesRequest, ListTaskDefinitionFamiliesResult]): Future[ListTaskDefinitionFamiliesResult] = ???

  override def listTaskDefinitionFamiliesAsync(): Future[ListTaskDefinitionFamiliesResult] = ???

  override def listTaskDefinitionFamiliesAsync(asyncHandler: AsyncHandler[ListTaskDefinitionFamiliesRequest, ListTaskDefinitionFamiliesResult]): Future[ListTaskDefinitionFamiliesResult] = ???

  override def listTaskDefinitionsAsync(listTaskDefinitionsRequest: ListTaskDefinitionsRequest): Future[ListTaskDefinitionsResult] = ???

  override def listTaskDefinitionsAsync(listTaskDefinitionsRequest: ListTaskDefinitionsRequest, asyncHandler: AsyncHandler[ListTaskDefinitionsRequest, ListTaskDefinitionsResult]): Future[ListTaskDefinitionsResult] = ???

  override def listTaskDefinitionsAsync(): Future[ListTaskDefinitionsResult] = ???

  override def listTaskDefinitionsAsync(asyncHandler: AsyncHandler[ListTaskDefinitionsRequest, ListTaskDefinitionsResult]): Future[ListTaskDefinitionsResult] = ???

  override def listTasksAsync(listTasksRequest: ListTasksRequest): Future[ListTasksResult] = ???

  override def listTasksAsync(listTasksRequest: ListTasksRequest, asyncHandler: AsyncHandler[ListTasksRequest, ListTasksResult]): Future[ListTasksResult] = ???

  override def listTasksAsync(): Future[ListTasksResult] = ???

  override def listTasksAsync(asyncHandler: AsyncHandler[ListTasksRequest, ListTasksResult]): Future[ListTasksResult] = ???

  override def putAccountSettingAsync(putAccountSettingRequest: PutAccountSettingRequest): Future[PutAccountSettingResult] = ???

  override def putAccountSettingAsync(putAccountSettingRequest: PutAccountSettingRequest, asyncHandler: AsyncHandler[PutAccountSettingRequest, PutAccountSettingResult]): Future[PutAccountSettingResult] = ???

  override def putAttributesAsync(putAttributesRequest: PutAttributesRequest): Future[PutAttributesResult] = ???

  override def putAttributesAsync(putAttributesRequest: PutAttributesRequest, asyncHandler: AsyncHandler[PutAttributesRequest, PutAttributesResult]): Future[PutAttributesResult] = ???

  override def registerContainerInstanceAsync(registerContainerInstanceRequest: RegisterContainerInstanceRequest): Future[RegisterContainerInstanceResult] = ???

  override def registerContainerInstanceAsync(registerContainerInstanceRequest: RegisterContainerInstanceRequest, asyncHandler: AsyncHandler[RegisterContainerInstanceRequest, RegisterContainerInstanceResult]): Future[RegisterContainerInstanceResult] = ???

  override def registerTaskDefinitionAsync(registerTaskDefinitionRequest: RegisterTaskDefinitionRequest): Future[RegisterTaskDefinitionResult] = ???

  override def registerTaskDefinitionAsync(registerTaskDefinitionRequest: RegisterTaskDefinitionRequest, asyncHandler: AsyncHandler[RegisterTaskDefinitionRequest, RegisterTaskDefinitionResult]): Future[RegisterTaskDefinitionResult] = ???

  override def runTaskAsync(runTaskRequest: RunTaskRequest): Future[RunTaskResult] = ???

  override def runTaskAsync(runTaskRequest: RunTaskRequest, asyncHandler: AsyncHandler[RunTaskRequest, RunTaskResult]): Future[RunTaskResult] = ???

  override def startTaskAsync(startTaskRequest: StartTaskRequest): Future[StartTaskResult] = ???

  override def startTaskAsync(startTaskRequest: StartTaskRequest, asyncHandler: AsyncHandler[StartTaskRequest, StartTaskResult]): Future[StartTaskResult] = ???

  override def stopTaskAsync(stopTaskRequest: StopTaskRequest): Future[StopTaskResult] = ???

  override def stopTaskAsync(stopTaskRequest: StopTaskRequest, asyncHandler: AsyncHandler[StopTaskRequest, StopTaskResult]): Future[StopTaskResult] = ???

  override def submitContainerStateChangeAsync(submitContainerStateChangeRequest: SubmitContainerStateChangeRequest): Future[SubmitContainerStateChangeResult] = ???

  override def submitContainerStateChangeAsync(submitContainerStateChangeRequest: SubmitContainerStateChangeRequest, asyncHandler: AsyncHandler[SubmitContainerStateChangeRequest, SubmitContainerStateChangeResult]): Future[SubmitContainerStateChangeResult] = ???

  override def submitContainerStateChangeAsync(): Future[SubmitContainerStateChangeResult] = ???

  override def submitContainerStateChangeAsync(asyncHandler: AsyncHandler[SubmitContainerStateChangeRequest, SubmitContainerStateChangeResult]): Future[SubmitContainerStateChangeResult] = ???

  override def submitTaskStateChangeAsync(submitTaskStateChangeRequest: SubmitTaskStateChangeRequest): Future[SubmitTaskStateChangeResult] = ???

  override def submitTaskStateChangeAsync(submitTaskStateChangeRequest: SubmitTaskStateChangeRequest, asyncHandler: AsyncHandler[SubmitTaskStateChangeRequest, SubmitTaskStateChangeResult]): Future[SubmitTaskStateChangeResult] = ???

  override def tagResourceAsync(tagResourceRequest: TagResourceRequest): Future[TagResourceResult] = ???

  override def tagResourceAsync(tagResourceRequest: TagResourceRequest, asyncHandler: AsyncHandler[TagResourceRequest, TagResourceResult]): Future[TagResourceResult] = ???

  override def untagResourceAsync(untagResourceRequest: UntagResourceRequest): Future[UntagResourceResult] = ???

  override def untagResourceAsync(untagResourceRequest: UntagResourceRequest, asyncHandler: AsyncHandler[UntagResourceRequest, UntagResourceResult]): Future[UntagResourceResult] = ???

  override def updateContainerAgentAsync(updateContainerAgentRequest: UpdateContainerAgentRequest): Future[UpdateContainerAgentResult] = ???

  override def updateContainerAgentAsync(updateContainerAgentRequest: UpdateContainerAgentRequest, asyncHandler: AsyncHandler[UpdateContainerAgentRequest, UpdateContainerAgentResult]): Future[UpdateContainerAgentResult] = ???

  override def updateContainerInstancesStateAsync(updateContainerInstancesStateRequest: UpdateContainerInstancesStateRequest): Future[UpdateContainerInstancesStateResult] = ???

  override def updateContainerInstancesStateAsync(updateContainerInstancesStateRequest: UpdateContainerInstancesStateRequest, asyncHandler: AsyncHandler[UpdateContainerInstancesStateRequest, UpdateContainerInstancesStateResult]): Future[UpdateContainerInstancesStateResult] = ???

  override def updateServiceAsync(updateServiceRequest: UpdateServiceRequest): Future[UpdateServiceResult] = ???

  override def updateServiceAsync(updateServiceRequest: UpdateServiceRequest, asyncHandler: AsyncHandler[UpdateServiceRequest, UpdateServiceResult]): Future[UpdateServiceResult] = ???

  override def setEndpoint(endpoint: String): Unit = ???

  override def setRegion(region: Region): Unit = ???

  override def createCluster(createClusterRequest: CreateClusterRequest): CreateClusterResult = ???

  override def createCluster(): CreateClusterResult = ???

  override def createService(createServiceRequest: CreateServiceRequest): CreateServiceResult = ???

  override def deleteAccountSetting(deleteAccountSettingRequest: DeleteAccountSettingRequest): DeleteAccountSettingResult = ???

  override def deleteAttributes(deleteAttributesRequest: DeleteAttributesRequest): DeleteAttributesResult = ???

  override def deleteCluster(deleteClusterRequest: DeleteClusterRequest): DeleteClusterResult = ???

  override def deleteService(deleteServiceRequest: DeleteServiceRequest): DeleteServiceResult = ???

  override def deregisterContainerInstance(deregisterContainerInstanceRequest: DeregisterContainerInstanceRequest): DeregisterContainerInstanceResult = ???

  override def deregisterTaskDefinition(deregisterTaskDefinitionRequest: DeregisterTaskDefinitionRequest): DeregisterTaskDefinitionResult = ???

  override def describeClusters(describeClustersRequest: DescribeClustersRequest): DescribeClustersResult = ???

  override def describeClusters(): DescribeClustersResult = ???

  override def describeContainerInstances(describeContainerInstancesRequest: DescribeContainerInstancesRequest): DescribeContainerInstancesResult = ???

  override def describeServices(describeServicesRequest: DescribeServicesRequest): DescribeServicesResult = ???

  override def describeTaskDefinition(describeTaskDefinitionRequest: DescribeTaskDefinitionRequest): DescribeTaskDefinitionResult = ???

  override def describeTasks(describeTasksRequest: DescribeTasksRequest): DescribeTasksResult = ???

  override def discoverPollEndpoint(discoverPollEndpointRequest: DiscoverPollEndpointRequest): DiscoverPollEndpointResult = ???

  override def discoverPollEndpoint(): DiscoverPollEndpointResult = ???

  override def listAccountSettings(listAccountSettingsRequest: ListAccountSettingsRequest): ListAccountSettingsResult = ???

  override def listAttributes(listAttributesRequest: ListAttributesRequest): ListAttributesResult = ???

  override def listClusters(listClustersRequest: ListClustersRequest): ListClustersResult = ???

  override def listClusters(): ListClustersResult = ???

  override def listContainerInstances(listContainerInstancesRequest: ListContainerInstancesRequest): ListContainerInstancesResult = ???

  override def listContainerInstances(): ListContainerInstancesResult = ???

  override def listServices(listServicesRequest: ListServicesRequest): ListServicesResult = ???

  override def listServices(): ListServicesResult = ???

  override def listTagsForResource(listTagsForResourceRequest: ListTagsForResourceRequest): ListTagsForResourceResult = ???

  override def listTaskDefinitionFamilies(listTaskDefinitionFamiliesRequest: ListTaskDefinitionFamiliesRequest): ListTaskDefinitionFamiliesResult = ???

  override def listTaskDefinitionFamilies(): ListTaskDefinitionFamiliesResult = ???

  override def listTaskDefinitions(listTaskDefinitionsRequest: ListTaskDefinitionsRequest): ListTaskDefinitionsResult = ???

  override def listTaskDefinitions(): ListTaskDefinitionsResult = ???

  override def listTasks(listTasksRequest: ListTasksRequest): ListTasksResult = ???

  override def listTasks(): ListTasksResult = ???

  override def putAccountSetting(putAccountSettingRequest: PutAccountSettingRequest): PutAccountSettingResult = ???

  override def putAttributes(putAttributesRequest: PutAttributesRequest): PutAttributesResult = ???

  override def registerContainerInstance(registerContainerInstanceRequest: RegisterContainerInstanceRequest): RegisterContainerInstanceResult = ???

  override def registerTaskDefinition(registerTaskDefinitionRequest: RegisterTaskDefinitionRequest): RegisterTaskDefinitionResult = ???

  override def runTask(runTaskRequest: RunTaskRequest): RunTaskResult = ???

  override def startTask(startTaskRequest: StartTaskRequest): StartTaskResult = ???

  override def stopTask(stopTaskRequest: StopTaskRequest): StopTaskResult = ???

  override def submitContainerStateChange(submitContainerStateChangeRequest: SubmitContainerStateChangeRequest): SubmitContainerStateChangeResult = ???

  override def submitContainerStateChange(): SubmitContainerStateChangeResult = ???

  override def submitTaskStateChange(submitTaskStateChangeRequest: SubmitTaskStateChangeRequest): SubmitTaskStateChangeResult = ???

  override def tagResource(tagResourceRequest: TagResourceRequest): TagResourceResult = ???

  override def untagResource(untagResourceRequest: UntagResourceRequest): UntagResourceResult = ???

  override def updateContainerAgent(updateContainerAgentRequest: UpdateContainerAgentRequest): UpdateContainerAgentResult = ???

  override def updateContainerInstancesState(updateContainerInstancesStateRequest: UpdateContainerInstancesStateRequest): UpdateContainerInstancesStateResult = ???

  override def updateService(updateServiceRequest: UpdateServiceRequest): UpdateServiceResult = ???

  override def shutdown(): Unit = ???

  override def getCachedResponseMetadata(request: AmazonWebServiceRequest): ResponseMetadata = ???

  override def waiters(): AmazonECSWaiters = ???
}
