ThisBuild / organization := "Dwolla"
ThisBuild / homepage := Option(url("https://github.com/Dwolla/autoscaling-ecs-draining-lambda"))
ThisBuild / tlCiDependencyGraphJob := false
ThisBuild / scalaVersion := "3.3.0"
ThisBuild / tlJdkRelease := Option(17)
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.corretto("17"))
ThisBuild / mergifyRequiredJobs ++= Seq("validate-steward")
ThisBuild / mergifyStewardConfig ~= { _.map(_.copy(
  author = "dwolla-oss-scala-steward[bot]",
  mergeMinors = true,
))}
topLevelDirectory := None

lazy val `smithy4s-generated` = project
  .in(file("smithy4s"))
  .settings(
    libraryDependencies ++= {
      Seq(
        "com.disneystreaming.smithy4s" %% "smithy4s-http4s" % smithy4sVersion.value,
        "com.disneystreaming.smithy4s" %% "smithy4s-aws-http4s" % smithy4sVersion.value,
        "com.disneystreaming.smithy" % "aws-ecs-spec" % "2023.02.10",
      )
    },
    scalacOptions ~= (_.filterNot(s => s.startsWith("-Ywarn") || s.startsWith("-Xlint") || s.startsWith("-W") || s.equals("-Xfatal-warnings"))),
  )
  .enablePlugins(
    Smithy4sCodegenPlugin,
  )

lazy val `autoscaling-ecs-draining-lambda` = project
  .in(file("."))
  .settings(
    libraryDependencies ++= {
      Seq(
        "com.dwolla" %% "fs2-utils" % "3.0.0-RC2",
        "org.typelevel" %% "feral-lambda" % "0.2.3",
        "org.typelevel" %% "log4cats-core" % "2.6.0",
        "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
        "org.http4s" %% "http4s-ember-client" % "0.23.21",
        "software.amazon.awssdk" % "autoscaling" % "2.20.69",
        "software.amazon.awssdk" % "sns" % "2.20.69",
        "io.circe" %% "circe-parser" % "0.14.5",
        "io.monix" %% "newtypes-core" % "0.2.3",
        "io.monix" %% "newtypes-circe-v0-14" % "0.2.3",
        "org.typelevel" %% "cats-effect-testkit" % "3.5.1" % Test,
        "org.typelevel" %% "munit-cats-effect" % "2.0.0-M3" % Test,
        "org.scalameta" %% "munit-scalacheck" % "1.0.0-M8" % Test,
        "org.typelevel" %% "scalacheck-effect-munit" % "2.0.0-M2" % Test,
        "org.typelevel" %% "log4cats-noop" % "2.6.0" % Test,
        "io.circe" %% "circe-literal" % "0.14.5" % Test,
        "io.circe" %% "circe-testing" % "0.14.5" % Test,
        "com.47deg" %% "scalacheck-toolbox-datetime" % "0.7.0" % Test exclude("joda-time", "joda-time"),
        "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2" % Test,
      )
    },
    topLevelDirectory := None,
  )
  .dependsOn(`smithy4s-generated`)
  .enablePlugins(
    UniversalPlugin,
    JavaAppPackaging,
    ServerlessDeployPlugin,
    GitVersioning,
  )
