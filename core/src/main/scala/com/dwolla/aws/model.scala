package com.dwolla.aws

import monix.newtypes.NewtypeWrapped

type AccountId = AccountId.Type
object AccountId extends NewtypeWrapped[String]

type TagName = TagName.Type
object TagName extends NewtypeWrapped[String]
type TagValue = TagValue.Type
object TagValue extends NewtypeWrapped[String]

case class Tag(name: TagName,
               value: TagValue)
