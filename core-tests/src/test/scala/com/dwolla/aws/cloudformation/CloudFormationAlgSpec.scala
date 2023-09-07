package com.dwolla.aws.cloudformation

import cats.effect.*
import cats.effect.std.Dispatcher
import com.dwolla.aws
import com.dwolla.aws.cloudformation.given
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.Arbitrary
import org.scalacheck.effect.PropF.forAllF
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.noop.NoOpFactory
import software.amazon.awssdk.services.cloudformation.CloudFormationAsyncClient
import software.amazon.awssdk.services.cloudformation.model.{CloudFormationException, DescribeStackResourceRequest, DescribeStackResourceResponse, StackResourceDetail}

import java.util.concurrent.CompletableFuture
import scala.jdk.CollectionConverters.*

class CloudFormationAlgSpec
  extends CatsEffectSuite
    with ScalaCheckEffectSuite {

  given LoggerFactory[IO] = NoOpFactory[IO]

  test("CloudFormationAlg should return the physical resource ID for the given parameters, if it can") {
    forAllF { (stack: StackArn,
               logicalResourceId: LogicalResourceId,
               maybePhysicalResourceId: Option[PhysicalResourceId],
              ) =>
      Dispatcher.sequential[IO].use { dispatcher =>
        val cfn = new CloudFormationAsyncClient {
          override def serviceName(): String = "FakeCloudFormationAsyncClient"
          override def close(): Unit = ()

          override def describeStackResource(req: DescribeStackResourceRequest): CompletableFuture[DescribeStackResourceResponse] =
            dispatcher.unsafeToCompletableFuture {
              if (req.stackName() == stack.value && req.logicalResourceId() == logicalResourceId.value) {
                maybePhysicalResourceId
                  .fold {
                    IO.raiseError[DescribeStackResourceResponse] {
                      CloudFormationException.builder()
                        .message(s"Resource ${logicalResourceId.value} does not exist for stack ${stack.value}")
                        .build()
                    }
                  } { physicalResourceId =>
                    IO.pure {
                      DescribeStackResourceResponse.builder()
                        .stackResourceDetail {
                          StackResourceDetail.builder()
                            .stackId(stack.value)
                            .logicalResourceId(logicalResourceId.value)
                            .physicalResourceId(physicalResourceId.value)
                            .build()
                        }
                        .build()
                    }
                  }
              } else {
                IO.raiseError[DescribeStackResourceResponse] {
                  CloudFormationException.builder()
                    .message(s"Stack '${stack.value}' does not exist")
                    .build()
                }
              }
            }
        }

        for {
          output <- CloudFormationAlg[IO](cfn).physicalResourceIdFor(stack, logicalResourceId)
        } yield {
          assertEquals(output, maybePhysicalResourceId)
        }
      }
    }
  }
}
