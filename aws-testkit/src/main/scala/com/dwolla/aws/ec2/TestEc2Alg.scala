package com.dwolla.aws.ec2

import cats.effect.*
import com.dwolla.aws.Tag

abstract class TestEc2Alg extends Ec2Alg[IO] {
  override def getTagsForInstance(id: Ec2InstanceId): IO[List[Tag]] = IO.raiseError(new NotImplementedError)
}
