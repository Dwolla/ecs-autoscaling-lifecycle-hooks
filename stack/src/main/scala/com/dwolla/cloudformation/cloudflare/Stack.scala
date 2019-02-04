package com.dwolla.cloudformation.cloudflare

import com.monsanto.arch.cloudformation.model._
import com.monsanto.arch.cloudformation.model.resource._

object Stack {
  def template(): Template = {
    val role = `AWS::IAM::Role`("Role",
      AssumeRolePolicyDocument = PolicyDocument(Seq(
        PolicyStatement(
          Effect = "Allow",
          Principal = Option(DefinedPrincipal(Map("Service" → Seq("lambda.amazonaws.com")))),
          Action = Seq("sts:AssumeRole")
        )
      )),
      Policies = Option(Seq(
        Policy("Policy",
          PolicyDocument(Seq(
            PolicyStatement(
              Effect = "Allow",
              Action = Seq(
                "logs:CreateLogGroup",
                "logs:CreateLogStream",
                "logs:PutLogEvents"
              ),
              Resource = Option("arn:aws:logs:*:*:*")
            ),
            PolicyStatement(
              Effect = "Allow",
              Action = Seq(
                "route53:GetHostedZone"
              ),
              Resource = Option("*")
            )
          ))
        )
      ))
    )

    val s3Bucket = StringParameter("S3Bucket", "bucket where Lambda code can be found")
    val s3Key = StringParameter("S3Key", "key where Lambda code can be found")

    val key = `AWS::KMS::Key`("Key",
      Option("Encryption key protecting secrets for the Cloudflare lambda"),
      Enabled = Option(true),
      EnableKeyRotation = Option(true),
      KeyPolicy = PolicyDocument(
        Seq(
          PolicyStatement(
            Sid = Option("AllowDataEncrypterToEncrypt"),
            Effect = "Allow",
            Principal = Option(DefinedPrincipal(Map("AWS" → Seq(`Fn::Sub`("arn:aws:iam::${AWS::AccountId}:role/DataEncrypter"))))),
            Action = Seq(
              "kms:Encrypt",
              "kms:ReEncrypt",
              "kms:Describe*",
              "kms:Get*",
              "kms:List*",
            ),
            Resource = Option("*")
          ),
          PolicyStatement(
            Sid = Option("AllowLambdaToDecrypt"),
            Effect = "Allow",
            Principal = Option(DefinedPrincipal(Map("AWS" → Seq(`Fn::GetAtt`(Seq(role.name, "Arn")))))),
            Action = Seq(
              "kms:Decrypt",
              "kms:DescribeKey"
            ),
            Resource = Option("*")
          ),
          PolicyStatement(
            Sid = Option("CloudFormationDeploymentRoleOwnsKey"),
            Effect = "Allow",
            Principal = Option(DefinedPrincipal(Map("AWS" → Seq(`Fn::Sub`("arn:aws:iam::${AWS::AccountId}:role/cloudformation/deployer/cloudformation-deployer"))))),
            Action = Seq(
              "kms:Create*",
              "kms:Describe*",
              "kms:Enable*",
              "kms:List*",
              "kms:Put*",
              "kms:Update*",
              "kms:Revoke*",
              "kms:Disable*",
              "kms:Get*",
              "kms:Delete*",
              "kms:ScheduleKeyDeletion",
              "kms:CancelKeyDeletion"
            ),
            Resource = Option("*")
          )
        )
      )
    )

    val alias: `AWS::KMS::Alias` = ???

    val lambda: `AWS::Lambda::Function` = ???

    val template: Template = ???

    template
  }
}
