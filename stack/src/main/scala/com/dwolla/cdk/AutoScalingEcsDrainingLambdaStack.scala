package com.dwolla.cdk

import cats.effect._
import cats.implicits._
import com.dwolla.autoscaling.ecs.draining.TerminationEventHandler
import software.amazon.awscdk.services.iam._
import software.amazon.awscdk.services.lambda._
import software.amazon.awscdk.services.s3.{Bucket, BucketImportProps}
import software.amazon.awscdk.services.sns._
import software.amazon.awscdk.{App => AwsApp, _}

import scala.collection.JavaConverters._

object AutoScalingEcsDrainingLambdaStack {
  def apply[F[_]](parent: AwsApp, name: String, props: Option[StackProps] = None)
                 (implicit F: Sync[F]): F[Stack] =
    for {
      stack <- F.delay(new Stack(parent, name, props.orNull))
      s3Bucket <- F.delay(new Parameter(stack, "S3Bucket", ParameterProps.builder().withType("String").withDescription("bucket where Lambda code can be found").build()))
      s3Key <- F.delay(new Parameter(stack, "S3Key", ParameterProps.builder().withType("String").withDescription("key where Lambda code can be found").build()))
      dwollaCodeBucket <- F.delay(Bucket.import_(stack, "code-bucket", BucketImportProps.builder().withBucketName(s3Bucket.getValueAsString).build()))
      lambdaInlinePolicyStatement <- F.delay(new PolicyStatement()
        .addActions(
          "autoscaling:CompleteLifecycleAction",
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents",
          "ecs:ListClusters",
          "ecs:ListContainerInstances",
          "ecs:DescribeContainerInstances",
          "ecs:UpdateContainerInstancesState",
          "sns:Publish",
        )
        .addAllResources()
        .allow())
      lambdaExecutionRole <- F.delay(new Role(stack, "LambdaExecutionRole",
        RoleProps.builder()
          .withInlinePolicies(Map("lambda-inline" -> new PolicyDocument().addStatement(lambdaInlinePolicyStatement)).asJava)
          .withManagedPolicyArns(List("arn:aws:iam::aws:policy/service-role/AutoScalingNotificationAccessRole").asJava)
          .withAssumedBy(new ServicePrincipal("lambda.amazonaws.com"))
          .build()
      ))
      snsLambdaRole <- F.delay(new Role(stack, "SnsLambdaRole",
        RoleProps.builder()
          .withManagedPolicyArns(List("arn:aws:iam::aws:policy/service-role/AutoScalingNotificationAccessRole").asJava)
          .withAssumedBy(new ServicePrincipal("autoscaling.amazonaws.com"))
          .build()
      ))
      function <- F.delay(new Function(stack,
        "LambdaFunctionForAutoScalingEventHook",
        FunctionProps.builder()
          .withCode(Code.bucket(dwollaCodeBucket, s3Key.getValueAsString))
          .withHandler(classOf[TerminationEventHandler].getCanonicalName)
          .withRuntime(Runtime.JAVA8)
          .withRole(lambdaExecutionRole)
          .withMemorySize(1024)
          .withTimeout(60)
          .build()))
      autoScalingSnsTopic <- F.delay(new Topic(stack, "AutoScalingSnsTopic",
        TopicProps.builder().withDisplayName("AutoScalingEcsDrainingNotification").build()
      ))
      _ <- F.delay(new Subscription(stack, "AutoScalingSnsTopicSubscription",
        SubscriptionProps.builder()
          .withEndpoint(function.getFunctionArn)
          .withProtocol(SubscriptionProtocol.Lambda)
          .withTopic(autoScalingSnsTopic)
          .build()
      ))
      _ <- F.delay( new CfnPermission(stack, "LambdaInvokePermission",
        CfnPermissionProps.builder()
          .withFunctionName(function.getFunctionName)
          .withAction("lambda:InvokeFunction")
          .withPrincipal("sns.amazonaws.com")
          .withSourceArn(autoScalingSnsTopic.getTopicArn)
          .build()
      ))
      _ <- F.delay(new Output(stack, "SNSTopicForASG",
        OutputProps.builder()
          .withExport("SnsTopicForAutoScalingNotificationsToDrainEcsInstances")
          .withValue(autoScalingSnsTopic.getTopicArn)
          .withDescription("Topic used by Auto Scaling to send notifications when instance state is changing")
          .build()))
      _ <- F.delay(new Output(stack, "SnsLambdaRoleArn",
        OutputProps.builder()
          .withExport("IamRoleForAutoScalingNotificationsToDrainEcsInstances")
          .withValue(snsLambdaRole.getRoleArn)
          .withDescription("IAM Role to allow Auto Scaling to assume AutoScalingNotificationAccessRole")
          .build()))

    } yield stack
}
