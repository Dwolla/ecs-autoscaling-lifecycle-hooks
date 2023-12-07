package com.dwolla.aws.ec2

import cats.tagless.aop.*
import cats.*
import com.amazonaws.ec2.*
import natchez.*
import com.dwolla.aws.TraceableValueInstances.given

private def traceableAdvice[A: TraceableValue](name: String, a: A): Aspect.Advice[Eval, TraceableValue] =
  Aspect.Advice.byValue[TraceableValue, A](name, a)

given Aspect[EC2, TraceableValue, TraceableValue] = new Aspect[EC2, TraceableValue, TraceableValue] {
  override def weave[F[_]](af: EC2[F]): EC2[[A] =>> Aspect.Weave[F, TraceableValue, TraceableValue, A]] =
    new EC2[[A] =>> Aspect.Weave[F, TraceableValue, TraceableValue, A]] {
      override def describeInstances(dryRun: Boolean,
                                     filters: Option[List[Filter]],
                                     instanceIds: Option[List[InstanceId]],
                                     nextToken: Option[String]): Aspect.Weave[F, TraceableValue, TraceableValue, DescribeInstancesResult] =
        Aspect.Weave[F, TraceableValue, TraceableValue, DescribeInstancesResult](
          "EC2",
          List(List(
            traceableAdvice("dryRun", dryRun),
            traceableAdvice("filters", filters),
            traceableAdvice("instanceIds", instanceIds),
            traceableAdvice("nextToken", nextToken),
          )),
          Aspect.Advice("describeInstances", af.describeInstances(dryRun, filters, instanceIds, nextToken))
        )
    }

  override def mapK[F[_], G[_]](af: EC2[F])
                               (fk: F ~> G): EC2[G] =
    new EC2[G] {
      override def describeInstances(dryRun: Boolean,
                                     filters: Option[List[Filter]],
                                     instanceIds: Option[List[InstanceId]],
                                     nextToken: Option[String]): G[DescribeInstancesResult] =
        fk(af.describeInstances(dryRun, filters, instanceIds, nextToken))
    }
}
