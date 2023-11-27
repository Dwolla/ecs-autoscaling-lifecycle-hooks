package com.dwolla.aws.ec2

import cats.effect.*
import com.amazonaws.ec2.InstanceId
import com.dwolla.aws.Tag

abstract class TestEc2Alg extends Ec2Alg[IO] {
  override def getTagsForInstance(id: InstanceId): IO[List[Tag]] = IO.raiseError(new NotImplementedError)
}
