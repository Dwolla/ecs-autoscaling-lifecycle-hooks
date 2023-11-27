package com.dwolla.aws.cloudformation

import cats.effect.*
import cats.syntax.all.*
import com.amazonaws.cloudformation.*
import com.dwolla.aws.cloudformation.given
import com.dwolla.aws.given
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.Arbitrary
import org.scalacheck.effect.PropF.forAllF
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.noop.NoOpFactory
import smithy4s.Timestamp
import smithy4s.http.UnknownErrorResponse

class CloudFormationAlgSpec
  extends CatsEffectSuite
    with ScalaCheckEffectSuite {

  given LoggerFactory[IO] = NoOpFactory[IO]

  test("CloudFormationAlg should return the physical resource ID for the given parameters, if it can") {
    forAllF { (arbStack: StackArn,
               arbLogicalResourceId: LogicalResourceId,
               maybePhysicalResourceId: Option[PhysicalResourceId],
               resourceType: ResourceType,
               timestamp: Timestamp,
               resourceStatus: ResourceStatus,
              ) =>
      val cfn = new CloudFormation.Default[IO](new NotImplementedError().raiseError) {
        override def describeStackResources(stackName: Option[StackName],
                                            logicalResourceId: Option[LogicalResourceId],
                                            physicalResourceId: Option[PhysicalResourceId]): IO[DescribeStackResourcesOutput] = {
          if (stackName.exists(_.value == arbStack.value) && logicalResourceId.exists(_.value == arbLogicalResourceId.value)) {
            maybePhysicalResourceId
              .fold {
                DescribeStackResourcesOutput(None)
              } { physicalResourceId =>
                DescribeStackResourcesOutput(StackResource(
                  logicalResourceId = arbLogicalResourceId,
                  resourceType = resourceType,
                  timestamp = timestamp,
                  resourceStatus = resourceStatus,
                  stackId = StackId(arbStack.value).some,
                  physicalResourceId = physicalResourceId.some,
                ).pure[List].some)
              }.pure[IO]
          } else
            IO.raiseError[DescribeStackResourcesOutput] {
              UnknownErrorResponse(400, Map.empty, s"Stack with id ${arbStack.value} does not exist")
            }
        }
      }

      for {
        output <- CloudFormationAlg[IO](cfn).physicalResourceIdFor(arbStack, arbLogicalResourceId)
      } yield {
        assertEquals(output, maybePhysicalResourceId)
      }
    }
  }
}
