package com.dwolla.aws.sns

import java.util
import java.util.concurrent.Future

import com.amazonaws.{AmazonWebServiceRequest, ResponseMetadata}
import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.regions.Region
import com.amazonaws.services.sns.AmazonSNSAsync
import com.amazonaws.services.sns.model._

abstract class FakeSNSAsync extends AmazonSNSAsync {
  override def addPermissionAsync(addPermissionRequest: AddPermissionRequest): Future[AddPermissionResult] = ???

  override def addPermissionAsync(addPermissionRequest: AddPermissionRequest, asyncHandler: AsyncHandler[AddPermissionRequest, AddPermissionResult]): Future[AddPermissionResult] = ???

  override def addPermissionAsync(topicArn: String, label: String, aWSAccountIds: util.List[String], actionNames: util.List[String]): Future[AddPermissionResult] = ???

  override def addPermissionAsync(topicArn: String, label: String, aWSAccountIds: util.List[String], actionNames: util.List[String], asyncHandler: AsyncHandler[AddPermissionRequest, AddPermissionResult]): Future[AddPermissionResult] = ???

  override def checkIfPhoneNumberIsOptedOutAsync(checkIfPhoneNumberIsOptedOutRequest: CheckIfPhoneNumberIsOptedOutRequest): Future[CheckIfPhoneNumberIsOptedOutResult] = ???

  override def checkIfPhoneNumberIsOptedOutAsync(checkIfPhoneNumberIsOptedOutRequest: CheckIfPhoneNumberIsOptedOutRequest, asyncHandler: AsyncHandler[CheckIfPhoneNumberIsOptedOutRequest, CheckIfPhoneNumberIsOptedOutResult]): Future[CheckIfPhoneNumberIsOptedOutResult] = ???

  override def confirmSubscriptionAsync(confirmSubscriptionRequest: ConfirmSubscriptionRequest): Future[ConfirmSubscriptionResult] = ???

  override def confirmSubscriptionAsync(confirmSubscriptionRequest: ConfirmSubscriptionRequest, asyncHandler: AsyncHandler[ConfirmSubscriptionRequest, ConfirmSubscriptionResult]): Future[ConfirmSubscriptionResult] = ???

  override def confirmSubscriptionAsync(topicArn: String, token: String, authenticateOnUnsubscribe: String): Future[ConfirmSubscriptionResult] = ???

  override def confirmSubscriptionAsync(topicArn: String, token: String, authenticateOnUnsubscribe: String, asyncHandler: AsyncHandler[ConfirmSubscriptionRequest, ConfirmSubscriptionResult]): Future[ConfirmSubscriptionResult] = ???

  override def confirmSubscriptionAsync(topicArn: String, token: String): Future[ConfirmSubscriptionResult] = ???

  override def confirmSubscriptionAsync(topicArn: String, token: String, asyncHandler: AsyncHandler[ConfirmSubscriptionRequest, ConfirmSubscriptionResult]): Future[ConfirmSubscriptionResult] = ???

  override def createPlatformApplicationAsync(createPlatformApplicationRequest: CreatePlatformApplicationRequest): Future[CreatePlatformApplicationResult] = ???

  override def createPlatformApplicationAsync(createPlatformApplicationRequest: CreatePlatformApplicationRequest, asyncHandler: AsyncHandler[CreatePlatformApplicationRequest, CreatePlatformApplicationResult]): Future[CreatePlatformApplicationResult] = ???

  override def createPlatformEndpointAsync(createPlatformEndpointRequest: CreatePlatformEndpointRequest): Future[CreatePlatformEndpointResult] = ???

  override def createPlatformEndpointAsync(createPlatformEndpointRequest: CreatePlatformEndpointRequest, asyncHandler: AsyncHandler[CreatePlatformEndpointRequest, CreatePlatformEndpointResult]): Future[CreatePlatformEndpointResult] = ???

  override def createTopicAsync(createTopicRequest: CreateTopicRequest): Future[CreateTopicResult] = ???

  override def createTopicAsync(createTopicRequest: CreateTopicRequest, asyncHandler: AsyncHandler[CreateTopicRequest, CreateTopicResult]): Future[CreateTopicResult] = ???

  override def createTopicAsync(name: String): Future[CreateTopicResult] = ???

  override def createTopicAsync(name: String, asyncHandler: AsyncHandler[CreateTopicRequest, CreateTopicResult]): Future[CreateTopicResult] = ???

  override def deleteEndpointAsync(deleteEndpointRequest: DeleteEndpointRequest): Future[DeleteEndpointResult] = ???

  override def deleteEndpointAsync(deleteEndpointRequest: DeleteEndpointRequest, asyncHandler: AsyncHandler[DeleteEndpointRequest, DeleteEndpointResult]): Future[DeleteEndpointResult] = ???

  override def deletePlatformApplicationAsync(deletePlatformApplicationRequest: DeletePlatformApplicationRequest): Future[DeletePlatformApplicationResult] = ???

  override def deletePlatformApplicationAsync(deletePlatformApplicationRequest: DeletePlatformApplicationRequest, asyncHandler: AsyncHandler[DeletePlatformApplicationRequest, DeletePlatformApplicationResult]): Future[DeletePlatformApplicationResult] = ???

  override def deleteTopicAsync(deleteTopicRequest: DeleteTopicRequest): Future[DeleteTopicResult] = ???

  override def deleteTopicAsync(deleteTopicRequest: DeleteTopicRequest, asyncHandler: AsyncHandler[DeleteTopicRequest, DeleteTopicResult]): Future[DeleteTopicResult] = ???

  override def deleteTopicAsync(topicArn: String): Future[DeleteTopicResult] = ???

  override def deleteTopicAsync(topicArn: String, asyncHandler: AsyncHandler[DeleteTopicRequest, DeleteTopicResult]): Future[DeleteTopicResult] = ???

  override def getEndpointAttributesAsync(getEndpointAttributesRequest: GetEndpointAttributesRequest): Future[GetEndpointAttributesResult] = ???

  override def getEndpointAttributesAsync(getEndpointAttributesRequest: GetEndpointAttributesRequest, asyncHandler: AsyncHandler[GetEndpointAttributesRequest, GetEndpointAttributesResult]): Future[GetEndpointAttributesResult] = ???

  override def getPlatformApplicationAttributesAsync(getPlatformApplicationAttributesRequest: GetPlatformApplicationAttributesRequest): Future[GetPlatformApplicationAttributesResult] = ???

  override def getPlatformApplicationAttributesAsync(getPlatformApplicationAttributesRequest: GetPlatformApplicationAttributesRequest, asyncHandler: AsyncHandler[GetPlatformApplicationAttributesRequest, GetPlatformApplicationAttributesResult]): Future[GetPlatformApplicationAttributesResult] = ???

  override def getSMSAttributesAsync(getSMSAttributesRequest: GetSMSAttributesRequest): Future[GetSMSAttributesResult] = ???

  override def getSMSAttributesAsync(getSMSAttributesRequest: GetSMSAttributesRequest, asyncHandler: AsyncHandler[GetSMSAttributesRequest, GetSMSAttributesResult]): Future[GetSMSAttributesResult] = ???

  override def getSubscriptionAttributesAsync(getSubscriptionAttributesRequest: GetSubscriptionAttributesRequest): Future[GetSubscriptionAttributesResult] = ???

  override def getSubscriptionAttributesAsync(getSubscriptionAttributesRequest: GetSubscriptionAttributesRequest, asyncHandler: AsyncHandler[GetSubscriptionAttributesRequest, GetSubscriptionAttributesResult]): Future[GetSubscriptionAttributesResult] = ???

  override def getSubscriptionAttributesAsync(subscriptionArn: String): Future[GetSubscriptionAttributesResult] = ???

  override def getSubscriptionAttributesAsync(subscriptionArn: String, asyncHandler: AsyncHandler[GetSubscriptionAttributesRequest, GetSubscriptionAttributesResult]): Future[GetSubscriptionAttributesResult] = ???

  override def getTopicAttributesAsync(getTopicAttributesRequest: GetTopicAttributesRequest): Future[GetTopicAttributesResult] = ???

  override def getTopicAttributesAsync(getTopicAttributesRequest: GetTopicAttributesRequest, asyncHandler: AsyncHandler[GetTopicAttributesRequest, GetTopicAttributesResult]): Future[GetTopicAttributesResult] = ???

  override def getTopicAttributesAsync(topicArn: String): Future[GetTopicAttributesResult] = ???

  override def getTopicAttributesAsync(topicArn: String, asyncHandler: AsyncHandler[GetTopicAttributesRequest, GetTopicAttributesResult]): Future[GetTopicAttributesResult] = ???

  override def listEndpointsByPlatformApplicationAsync(listEndpointsByPlatformApplicationRequest: ListEndpointsByPlatformApplicationRequest): Future[ListEndpointsByPlatformApplicationResult] = ???

  override def listEndpointsByPlatformApplicationAsync(listEndpointsByPlatformApplicationRequest: ListEndpointsByPlatformApplicationRequest, asyncHandler: AsyncHandler[ListEndpointsByPlatformApplicationRequest, ListEndpointsByPlatformApplicationResult]): Future[ListEndpointsByPlatformApplicationResult] = ???

  override def listPhoneNumbersOptedOutAsync(listPhoneNumbersOptedOutRequest: ListPhoneNumbersOptedOutRequest): Future[ListPhoneNumbersOptedOutResult] = ???

  override def listPhoneNumbersOptedOutAsync(listPhoneNumbersOptedOutRequest: ListPhoneNumbersOptedOutRequest, asyncHandler: AsyncHandler[ListPhoneNumbersOptedOutRequest, ListPhoneNumbersOptedOutResult]): Future[ListPhoneNumbersOptedOutResult] = ???

  override def listPlatformApplicationsAsync(listPlatformApplicationsRequest: ListPlatformApplicationsRequest): Future[ListPlatformApplicationsResult] = ???

  override def listPlatformApplicationsAsync(listPlatformApplicationsRequest: ListPlatformApplicationsRequest, asyncHandler: AsyncHandler[ListPlatformApplicationsRequest, ListPlatformApplicationsResult]): Future[ListPlatformApplicationsResult] = ???

  override def listPlatformApplicationsAsync(): Future[ListPlatformApplicationsResult] = ???

  override def listPlatformApplicationsAsync(asyncHandler: AsyncHandler[ListPlatformApplicationsRequest, ListPlatformApplicationsResult]): Future[ListPlatformApplicationsResult] = ???

  override def listSubscriptionsAsync(listSubscriptionsRequest: ListSubscriptionsRequest): Future[ListSubscriptionsResult] = ???

  override def listSubscriptionsAsync(listSubscriptionsRequest: ListSubscriptionsRequest, asyncHandler: AsyncHandler[ListSubscriptionsRequest, ListSubscriptionsResult]): Future[ListSubscriptionsResult] = ???

  override def listSubscriptionsAsync(): Future[ListSubscriptionsResult] = ???

  override def listSubscriptionsAsync(asyncHandler: AsyncHandler[ListSubscriptionsRequest, ListSubscriptionsResult]): Future[ListSubscriptionsResult] = ???

  override def listSubscriptionsAsync(nextToken: String): Future[ListSubscriptionsResult] = ???

  override def listSubscriptionsAsync(nextToken: String, asyncHandler: AsyncHandler[ListSubscriptionsRequest, ListSubscriptionsResult]): Future[ListSubscriptionsResult] = ???

  override def listSubscriptionsByTopicAsync(listSubscriptionsByTopicRequest: ListSubscriptionsByTopicRequest): Future[ListSubscriptionsByTopicResult] = ???

  override def listSubscriptionsByTopicAsync(listSubscriptionsByTopicRequest: ListSubscriptionsByTopicRequest, asyncHandler: AsyncHandler[ListSubscriptionsByTopicRequest, ListSubscriptionsByTopicResult]): Future[ListSubscriptionsByTopicResult] = ???

  override def listSubscriptionsByTopicAsync(topicArn: String): Future[ListSubscriptionsByTopicResult] = ???

  override def listSubscriptionsByTopicAsync(topicArn: String, asyncHandler: AsyncHandler[ListSubscriptionsByTopicRequest, ListSubscriptionsByTopicResult]): Future[ListSubscriptionsByTopicResult] = ???

  override def listSubscriptionsByTopicAsync(topicArn: String, nextToken: String): Future[ListSubscriptionsByTopicResult] = ???

  override def listSubscriptionsByTopicAsync(topicArn: String, nextToken: String, asyncHandler: AsyncHandler[ListSubscriptionsByTopicRequest, ListSubscriptionsByTopicResult]): Future[ListSubscriptionsByTopicResult] = ???

  override def listTopicsAsync(listTopicsRequest: ListTopicsRequest): Future[ListTopicsResult] = ???

  override def listTopicsAsync(listTopicsRequest: ListTopicsRequest, asyncHandler: AsyncHandler[ListTopicsRequest, ListTopicsResult]): Future[ListTopicsResult] = ???

  override def listTopicsAsync(): Future[ListTopicsResult] = ???

  override def listTopicsAsync(asyncHandler: AsyncHandler[ListTopicsRequest, ListTopicsResult]): Future[ListTopicsResult] = ???

  override def listTopicsAsync(nextToken: String): Future[ListTopicsResult] = ???

  override def listTopicsAsync(nextToken: String, asyncHandler: AsyncHandler[ListTopicsRequest, ListTopicsResult]): Future[ListTopicsResult] = ???

  override def optInPhoneNumberAsync(optInPhoneNumberRequest: OptInPhoneNumberRequest): Future[OptInPhoneNumberResult] = ???

  override def optInPhoneNumberAsync(optInPhoneNumberRequest: OptInPhoneNumberRequest, asyncHandler: AsyncHandler[OptInPhoneNumberRequest, OptInPhoneNumberResult]): Future[OptInPhoneNumberResult] = ???

  override def publishAsync(publishRequest: PublishRequest): Future[PublishResult] = ???

  override def publishAsync(publishRequest: PublishRequest, asyncHandler: AsyncHandler[PublishRequest, PublishResult]): Future[PublishResult] = ???

  override def publishAsync(topicArn: String, message: String): Future[PublishResult] = ???

  override def publishAsync(topicArn: String, message: String, asyncHandler: AsyncHandler[PublishRequest, PublishResult]): Future[PublishResult] = ???

  override def publishAsync(topicArn: String, message: String, subject: String): Future[PublishResult] = ???

  override def publishAsync(topicArn: String, message: String, subject: String, asyncHandler: AsyncHandler[PublishRequest, PublishResult]): Future[PublishResult] = ???

  override def removePermissionAsync(removePermissionRequest: RemovePermissionRequest): Future[RemovePermissionResult] = ???

  override def removePermissionAsync(removePermissionRequest: RemovePermissionRequest, asyncHandler: AsyncHandler[RemovePermissionRequest, RemovePermissionResult]): Future[RemovePermissionResult] = ???

  override def removePermissionAsync(topicArn: String, label: String): Future[RemovePermissionResult] = ???

  override def removePermissionAsync(topicArn: String, label: String, asyncHandler: AsyncHandler[RemovePermissionRequest, RemovePermissionResult]): Future[RemovePermissionResult] = ???

  override def setEndpointAttributesAsync(setEndpointAttributesRequest: SetEndpointAttributesRequest): Future[SetEndpointAttributesResult] = ???

  override def setEndpointAttributesAsync(setEndpointAttributesRequest: SetEndpointAttributesRequest, asyncHandler: AsyncHandler[SetEndpointAttributesRequest, SetEndpointAttributesResult]): Future[SetEndpointAttributesResult] = ???

  override def setPlatformApplicationAttributesAsync(setPlatformApplicationAttributesRequest: SetPlatformApplicationAttributesRequest): Future[SetPlatformApplicationAttributesResult] = ???

  override def setPlatformApplicationAttributesAsync(setPlatformApplicationAttributesRequest: SetPlatformApplicationAttributesRequest, asyncHandler: AsyncHandler[SetPlatformApplicationAttributesRequest, SetPlatformApplicationAttributesResult]): Future[SetPlatformApplicationAttributesResult] = ???

  override def setSMSAttributesAsync(setSMSAttributesRequest: SetSMSAttributesRequest): Future[SetSMSAttributesResult] = ???

  override def setSMSAttributesAsync(setSMSAttributesRequest: SetSMSAttributesRequest, asyncHandler: AsyncHandler[SetSMSAttributesRequest, SetSMSAttributesResult]): Future[SetSMSAttributesResult] = ???

  override def setSubscriptionAttributesAsync(setSubscriptionAttributesRequest: SetSubscriptionAttributesRequest): Future[SetSubscriptionAttributesResult] = ???

  override def setSubscriptionAttributesAsync(setSubscriptionAttributesRequest: SetSubscriptionAttributesRequest, asyncHandler: AsyncHandler[SetSubscriptionAttributesRequest, SetSubscriptionAttributesResult]): Future[SetSubscriptionAttributesResult] = ???

  override def setSubscriptionAttributesAsync(subscriptionArn: String, attributeName: String, attributeValue: String): Future[SetSubscriptionAttributesResult] = ???

  override def setSubscriptionAttributesAsync(subscriptionArn: String, attributeName: String, attributeValue: String, asyncHandler: AsyncHandler[SetSubscriptionAttributesRequest, SetSubscriptionAttributesResult]): Future[SetSubscriptionAttributesResult] = ???

  override def setTopicAttributesAsync(setTopicAttributesRequest: SetTopicAttributesRequest): Future[SetTopicAttributesResult] = ???

  override def setTopicAttributesAsync(setTopicAttributesRequest: SetTopicAttributesRequest, asyncHandler: AsyncHandler[SetTopicAttributesRequest, SetTopicAttributesResult]): Future[SetTopicAttributesResult] = ???

  override def setTopicAttributesAsync(topicArn: String, attributeName: String, attributeValue: String): Future[SetTopicAttributesResult] = ???

  override def setTopicAttributesAsync(topicArn: String, attributeName: String, attributeValue: String, asyncHandler: AsyncHandler[SetTopicAttributesRequest, SetTopicAttributesResult]): Future[SetTopicAttributesResult] = ???

  override def subscribeAsync(subscribeRequest: SubscribeRequest): Future[SubscribeResult] = ???

  override def subscribeAsync(subscribeRequest: SubscribeRequest, asyncHandler: AsyncHandler[SubscribeRequest, SubscribeResult]): Future[SubscribeResult] = ???

  override def subscribeAsync(topicArn: String, protocol: String, endpoint: String): Future[SubscribeResult] = ???

  override def subscribeAsync(topicArn: String, protocol: String, endpoint: String, asyncHandler: AsyncHandler[SubscribeRequest, SubscribeResult]): Future[SubscribeResult] = ???

  override def unsubscribeAsync(unsubscribeRequest: UnsubscribeRequest): Future[UnsubscribeResult] = ???

  override def unsubscribeAsync(unsubscribeRequest: UnsubscribeRequest, asyncHandler: AsyncHandler[UnsubscribeRequest, UnsubscribeResult]): Future[UnsubscribeResult] = ???

  override def unsubscribeAsync(subscriptionArn: String): Future[UnsubscribeResult] = ???

  override def unsubscribeAsync(subscriptionArn: String, asyncHandler: AsyncHandler[UnsubscribeRequest, UnsubscribeResult]): Future[UnsubscribeResult] = ???

  override def setEndpoint(endpoint: String): Unit = ???

  override def setRegion(region: Region): Unit = ???

  override def addPermission(addPermissionRequest: AddPermissionRequest): AddPermissionResult = ???

  override def addPermission(topicArn: String, label: String, aWSAccountIds: util.List[String], actionNames: util.List[String]): AddPermissionResult = ???

  override def checkIfPhoneNumberIsOptedOut(checkIfPhoneNumberIsOptedOutRequest: CheckIfPhoneNumberIsOptedOutRequest): CheckIfPhoneNumberIsOptedOutResult = ???

  override def confirmSubscription(confirmSubscriptionRequest: ConfirmSubscriptionRequest): ConfirmSubscriptionResult = ???

  override def confirmSubscription(topicArn: String, token: String, authenticateOnUnsubscribe: String): ConfirmSubscriptionResult = ???

  override def confirmSubscription(topicArn: String, token: String): ConfirmSubscriptionResult = ???

  override def createPlatformApplication(createPlatformApplicationRequest: CreatePlatformApplicationRequest): CreatePlatformApplicationResult = ???

  override def createPlatformEndpoint(createPlatformEndpointRequest: CreatePlatformEndpointRequest): CreatePlatformEndpointResult = ???

  override def createTopic(createTopicRequest: CreateTopicRequest): CreateTopicResult = ???

  override def createTopic(name: String): CreateTopicResult = ???

  override def deleteEndpoint(deleteEndpointRequest: DeleteEndpointRequest): DeleteEndpointResult = ???

  override def deletePlatformApplication(deletePlatformApplicationRequest: DeletePlatformApplicationRequest): DeletePlatformApplicationResult = ???

  override def deleteTopic(deleteTopicRequest: DeleteTopicRequest): DeleteTopicResult = ???

  override def deleteTopic(topicArn: String): DeleteTopicResult = ???

  override def getEndpointAttributes(getEndpointAttributesRequest: GetEndpointAttributesRequest): GetEndpointAttributesResult = ???

  override def getPlatformApplicationAttributes(getPlatformApplicationAttributesRequest: GetPlatformApplicationAttributesRequest): GetPlatformApplicationAttributesResult = ???

  override def getSMSAttributes(getSMSAttributesRequest: GetSMSAttributesRequest): GetSMSAttributesResult = ???

  override def getSubscriptionAttributes(getSubscriptionAttributesRequest: GetSubscriptionAttributesRequest): GetSubscriptionAttributesResult = ???

  override def getSubscriptionAttributes(subscriptionArn: String): GetSubscriptionAttributesResult = ???

  override def getTopicAttributes(getTopicAttributesRequest: GetTopicAttributesRequest): GetTopicAttributesResult = ???

  override def getTopicAttributes(topicArn: String): GetTopicAttributesResult = ???

  override def listEndpointsByPlatformApplication(listEndpointsByPlatformApplicationRequest: ListEndpointsByPlatformApplicationRequest): ListEndpointsByPlatformApplicationResult = ???

  override def listPhoneNumbersOptedOut(listPhoneNumbersOptedOutRequest: ListPhoneNumbersOptedOutRequest): ListPhoneNumbersOptedOutResult = ???

  override def listPlatformApplications(listPlatformApplicationsRequest: ListPlatformApplicationsRequest): ListPlatformApplicationsResult = ???

  override def listPlatformApplications(): ListPlatformApplicationsResult = ???

  override def listSubscriptions(listSubscriptionsRequest: ListSubscriptionsRequest): ListSubscriptionsResult = ???

  override def listSubscriptions(): ListSubscriptionsResult = ???

  override def listSubscriptions(nextToken: String): ListSubscriptionsResult = ???

  override def listSubscriptionsByTopic(listSubscriptionsByTopicRequest: ListSubscriptionsByTopicRequest): ListSubscriptionsByTopicResult = ???

  override def listSubscriptionsByTopic(topicArn: String): ListSubscriptionsByTopicResult = ???

  override def listSubscriptionsByTopic(topicArn: String, nextToken: String): ListSubscriptionsByTopicResult = ???

  override def listTopics(listTopicsRequest: ListTopicsRequest): ListTopicsResult = ???

  override def listTopics(): ListTopicsResult = ???

  override def listTopics(nextToken: String): ListTopicsResult = ???

  override def optInPhoneNumber(optInPhoneNumberRequest: OptInPhoneNumberRequest): OptInPhoneNumberResult = ???

  override def publish(publishRequest: PublishRequest): PublishResult = ???

  override def publish(topicArn: String, message: String): PublishResult = ???

  override def publish(topicArn: String, message: String, subject: String): PublishResult = ???

  override def removePermission(removePermissionRequest: RemovePermissionRequest): RemovePermissionResult = ???

  override def removePermission(topicArn: String, label: String): RemovePermissionResult = ???

  override def setEndpointAttributes(setEndpointAttributesRequest: SetEndpointAttributesRequest): SetEndpointAttributesResult = ???

  override def setPlatformApplicationAttributes(setPlatformApplicationAttributesRequest: SetPlatformApplicationAttributesRequest): SetPlatformApplicationAttributesResult = ???

  override def setSMSAttributes(setSMSAttributesRequest: SetSMSAttributesRequest): SetSMSAttributesResult = ???

  override def setSubscriptionAttributes(setSubscriptionAttributesRequest: SetSubscriptionAttributesRequest): SetSubscriptionAttributesResult = ???

  override def setSubscriptionAttributes(subscriptionArn: String, attributeName: String, attributeValue: String): SetSubscriptionAttributesResult = ???

  override def setTopicAttributes(setTopicAttributesRequest: SetTopicAttributesRequest): SetTopicAttributesResult = ???

  override def setTopicAttributes(topicArn: String, attributeName: String, attributeValue: String): SetTopicAttributesResult = ???

  override def subscribe(subscribeRequest: SubscribeRequest): SubscribeResult = ???

  override def subscribe(topicArn: String, protocol: String, endpoint: String): SubscribeResult = ???

  override def unsubscribe(unsubscribeRequest: UnsubscribeRequest): UnsubscribeResult = ???

  override def unsubscribe(subscriptionArn: String): UnsubscribeResult = ???

  override def shutdown(): Unit = ???

  override def getCachedResponseMetadata(request: AmazonWebServiceRequest): ResponseMetadata = ???
}
