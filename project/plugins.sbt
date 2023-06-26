addSbtPlugin("org.typelevel" % "sbt-typelevel-no-publish" % "0.5.0-RC4")
addSbtPlugin("org.typelevel" % "sbt-typelevel-ci-release" % "0.5.0-RC4")
addSbtPlugin("org.typelevel" % "sbt-typelevel-settings" % "0.5.0-RC4")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")
addSbtPlugin("com.disneystreaming.smithy4s" % "smithy4s-sbt-codegen" % "0.17.10")

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
