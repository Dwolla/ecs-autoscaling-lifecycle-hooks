# Auto Scaling ECS Instance Draining Lambda

[![Travis](https://img.shields.io/travis/Dwolla/autoscaling-ecs-draining-lambda.svg?style=flat-square)](https://travis-ci.org/Dwolla/autoscaling-ecs-draining-lambda)
![license](https://img.shields.io/github/license/Dwolla/autoscaling-ecs-draining-lambda.svg?style=flat-square)

AWS Lambda function to use [Auto Scaling Lifecycle Hooks](https://docs.aws.amazon.com/autoscaling/ec2/userguide/lifecycle-hooks.html) to drain ECS instances when an Auto Scaling Group scales in.

Inspired by [“How to Automate Container Instance Draining in Amazon ECS”](https://aws.amazon.com/blogs/compute/how-to-automate-container-instance-draining-in-amazon-ecs/).

## Deploy

To deploy the stack, ensure the required IAM roles exist (`DataEncrypter` and `cloudformation/deployer/cloudformation-deployer`), then deploy with `sbt`:

```ShellSession
sbt -DAWS_ACCOUNT_ID={your-account-id} publish stack/deploy
```

The `publish` task comes from [Dwolla’s S3 sbt plugin](https://github.com/Dwolla/sbt-s3-publisher), and the stack/deploy task comes from [Dwolla’s CloudFormation sbt plugin](https://github.com/Dwolla/sbt-cloudformation-stack).
