package com.dwolla.aws.ec2

import cats.effect.*
import cats.effect.std.Dispatcher
import com.dwolla.aws.*
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.effect.PropF.forAllF
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.noop.NoOpFactory
import software.amazon.awssdk.services.ec2.Ec2AsyncClient
import software.amazon.awssdk.services.ec2.model.{DescribeInstancesRequest, DescribeInstancesResponse, Instance, Reservation}

import java.util.concurrent.CompletableFuture
import scala.jdk.CollectionConverters.*

class Ec2AlgSpec
  extends CatsEffectSuite
    with ScalaCheckEffectSuite
    with ArbitraryInstances {

  private implicit val loggerFactory: LoggerFactory[IO] = NoOpFactory[IO]

  test("Ec2Alg should retrieve the tags for an instance") {
    forAllF { (instance: Instance) =>
      Dispatcher.sequential[IO].use { dispatcher =>
        val fakeClient = new Ec2AsyncClient {
          override def serviceName(): String = "FakeEc2AsyncClient"
          override def close(): Unit = ()

          override def describeInstances(req: DescribeInstancesRequest): CompletableFuture[DescribeInstancesResponse] =
            dispatcher.unsafeToCompletableFuture {
              IO.pure {
                if (req.instanceIds().contains(instance.instanceId())) {
                  DescribeInstancesResponse
                    .builder()
                    .reservations(Reservation.builder().instances(instance).build())
                    .build()
                } else {
                  DescribeInstancesResponse
                    .builder()
                    .build()
                }
              }
            }
        }

        for {
          output <- Ec2Alg[IO](fakeClient).getTagsForInstance(Ec2InstanceId(instance.instanceId()))
        } yield {
          assertEquals(output, instance.tags().asScala.toList.map { tag => Tag(TagName(tag.key), TagValue(tag.value))})
        }
      }
    }
  }
}
