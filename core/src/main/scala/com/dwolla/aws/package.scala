package com.dwolla.aws

import monix.newtypes.NewtypeWrapped

type AccountId = AccountId.Type
object AccountId extends NewtypeWrapped[String]
