# ECS Auto Scaling Lifecycle Hooks

![CI](https://github.com/Dwolla/autoscaling-ecs-draining-lambda/actions/workflows/ci.yml/badge.svg)
![license](https://img.shields.io/github/license/Dwolla/autoscaling-ecs-draining-lambda.svg?style=flat-square)

AWS Lambda functions to use [Auto Scaling Lifecycle Hooks](https://docs.aws.amazon.com/autoscaling/ec2/userguide/lifecycle-hooks.html) with ECS.

Inspired by [“How to Automate Container Instance Draining in Amazon ECS”](https://aws.amazon.com/blogs/compute/how-to-automate-container-instance-draining-in-amazon-ecs/).

## Deploy

To deploy the stack, ensure the required IAM role exists (`cloudformation/deployer/cloudformation-deployer`), then deploy with `sbt`:

```ShellSession
sbt "deploy Admin"
```
