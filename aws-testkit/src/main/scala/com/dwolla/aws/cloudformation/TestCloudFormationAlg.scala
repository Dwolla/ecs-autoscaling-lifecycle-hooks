package com.dwolla.aws.cloudformation

import cats.effect.*
import com.amazonaws.cloudformation.*

abstract class TestCloudFormationAlg extends CloudFormationAlg[IO] {
  override def physicalResourceIdFor(stack: StackArn, logicalResourceId: LogicalResourceId): IO[Option[PhysicalResourceId]] = IO.raiseError(new NotImplementedError)
}
