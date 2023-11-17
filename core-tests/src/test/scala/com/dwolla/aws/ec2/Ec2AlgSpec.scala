package com.dwolla.aws.ec2

import cats.effect.*
import cats.syntax.all.*
import com.amazonaws.ec2.{EC2, Instance, DescribeInstancesResult, Filter, InstanceId, Reservation}
import com.dwolla.aws.*
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.effect.PropF.forAllF
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.noop.NoOpFactory
import com.dwolla.aws

import com.dwolla.aws.ec2.given

class Ec2AlgSpec
  extends CatsEffectSuite
    with ScalaCheckEffectSuite {

  given LoggerFactory[IO] = NoOpFactory[IO]

  test("Ec2Alg should retrieve the tags for an instance") {
    forAllF { (instance: Instance) =>
      instance
        .instanceId
        .traverse { instanceId =>
          val fakeClient = new EC2.Default[IO](new NotImplementedError().raiseError) {
            override def describeInstances(dryRun: Boolean,
                                           maxResults: Int,
                                           filters: Option[List[Filter]],
                                           instanceIds: Option[List[InstanceId]],
                                           nextToken: Option[String]): IO[DescribeInstancesResult] =
              IO.pure {
                if (instanceIds.exists(_.contains(instanceId)))
                  DescribeInstancesResult(Reservation(instances = instance.pure[List].some).pure[List].some)
                else
                  DescribeInstancesResult()
              }
          }

          for {
            output <- Ec2Alg[IO](fakeClient).getTagsForInstance(InstanceId(instanceId))
          } yield {
            assertEquals(output,
              instance
                .tags
                .toList
                .flatten
                .map { tag =>
                  (tag.key.map(TagName.apply), tag.value.map(TagValue.apply)).mapN(Tag.apply)
                }
                .flattenOption)
          }
        }
        .map(_.getOrElse(()))
    }
  }
}
