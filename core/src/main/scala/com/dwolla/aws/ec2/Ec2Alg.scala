package com.dwolla.aws.ec2

import monix.newtypes.NewtypeWrapped

type Ec2InstanceId = Ec2InstanceId.Type
object Ec2InstanceId extends NewtypeWrapped[String]
