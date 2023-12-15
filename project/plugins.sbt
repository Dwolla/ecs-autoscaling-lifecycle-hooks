addSbtPlugin("org.typelevel" % "sbt-typelevel-ci" % "0.6.4")
addSbtPlugin("org.typelevel" % "sbt-typelevel-settings" % "0.6.4")
addSbtPlugin("org.typelevel" % "sbt-typelevel-mergify" % "0.6.4")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")
addSbtPlugin("com.disneystreaming.smithy4s" % "smithy4s-sbt-codegen" % "0.18.3")

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
