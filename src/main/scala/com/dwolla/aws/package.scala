package com.dwolla

import shapeless.tag._
import shapeless.tag

package object aws {
  type AccountId = String @@ AccountIdTag
  type RegionName = String @@ RegionNameTag

  val tagAccountId: String => AccountId = tag[AccountIdTag][String]
  val tagRegionName: String => RegionName = tag[RegionNameTag][String]
}

package aws {
  trait AccountIdTag
  trait RegionNameTag
}
