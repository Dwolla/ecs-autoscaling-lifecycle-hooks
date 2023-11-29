ThisBuild / organization := "Dwolla"
ThisBuild / homepage := Option(url("https://github.com/Dwolla/autoscaling-ecs-draining-lambda"))
ThisBuild / tlCiDependencyGraphJob := false
ThisBuild / scalaVersion := "3.3.1"
ThisBuild / tlJdkRelease := Option(17)
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.corretto("17"))
ThisBuild / githubWorkflowBuild += WorkflowStep.Sbt(name = Option("Package"), commands = List("autoscaling-ecs-draining-lambda/Universal/packageBin"))
ThisBuild / mergifyRequiredJobs ++= Seq("validate-steward")
ThisBuild / mergifyStewardConfig ~= { _.map(_.copy(
  author = "dwolla-oss-scala-steward[bot]",
  mergeMinors = true,
))}
topLevelDirectory := None
ThisBuild / scalacOptions += "-source:future"

lazy val `smithy4s-preprocessors` = project
  .in(file("smithy4s-preprocessors"))
  .settings(
    scalaVersion := "2.12.13", // 2.12 to match what SBT uses
    scalacOptions -= "-source:future",
    libraryDependencies ++= {
      Seq(
        "org.typelevel" %% "cats-core" % "2.10.0",
        "software.amazon.smithy" % "smithy-build" % smithy4s.codegen.BuildInfo.smithyVersion,
      )
    },
  )

lazy val `smithy4s-generated` = project
  .in(file("smithy4s"))
  .settings(
    libraryDependencies ++= {
      Seq(
        "com.disneystreaming.smithy4s" %% "smithy4s-http4s" % smithy4sVersion.value,
        "com.disneystreaming.smithy4s" %% "smithy4s-aws-http4s" % smithy4sVersion.value,
      )
    },
    smithy4sAwsSpecs ++= Seq(
      AWS.autoScaling,
      AWS.cloudformation,
      AWS.ec2,
      AWS.ecs,
      AWS.sns,
    ),
    scalacOptions += "-Wconf:any:s",
    Compile / smithy4sModelTransformers ++= List(
      "AutoscalingPreprocessor",
      "CloudformationPreprocessor",
      "Ec2Preprocessor",
      "EcsPreprocessor",
      "SnsPreprocessor",
    ),
    Compile / smithy4sAllDependenciesAsJars += (`smithy4s-preprocessors` / Compile / packageBin).value
  )
  .enablePlugins(
    Smithy4sCodegenPlugin,
  )

lazy val `autoscaling-ecs-core`: Project = project
  .in(file("core"))
  .settings(
    libraryDependencies ++= {
      Seq(
        "com.dwolla" %% "fs2-utils" % "3.0.0-RC2",
        "org.typelevel" %% "feral-lambda" % "0.2.4",
        "org.typelevel" %% "log4cats-core" % "2.6.0",
        "io.circe" %% "circe-parser" % "0.14.6",
        "io.monix" %% "newtypes-core" % "0.2.3",
        "io.monix" %% "newtypes-circe-v0-14" % "0.2.3",
        "org.tpolecat" %% "natchez-core" % "0.3.4",
        "org.typelevel" %% "cats-tagless-core" % "0.15.0",
        "org.typelevel" %% "alleycats-core" % "2.10.0",
        "com.dwolla" %% "natchez-tagless" % "0.2.4",
      )
    },
  )
  .dependsOn(
    `smithy4s-generated`,
  )

lazy val `core-tests` = project
  .in(file("core-tests"))
  .settings(
    libraryDependencies ++= {
      Seq(
        "org.http4s" %% "http4s-ember-client" % "0.23.24" % Test,
        "org.typelevel" %% "cats-effect-testkit" % "3.5.2" % Test,
        "org.typelevel" %% "munit-cats-effect" % "2.0.0-M4" % Test,
        "org.scalameta" %% "munit-scalacheck" % "1.0.0-M10" % Test,
        "org.typelevel" %% "scalacheck-effect-munit" % "2.0.0-M2" % Test,
        "org.typelevel" %% "log4cats-noop" % "2.6.0" % Test,
        "org.typelevel" %% "mouse" % "1.2.2" % Test,
        "org.tpolecat" %% "natchez-noop" % "0.3.4" % Test,
        "io.circe" %% "circe-literal" % "0.14.6" % Test,
        "io.circe" %% "circe-testing" % "0.14.6" % Test,
        "com.47deg" %% "scalacheck-toolbox-datetime" % "0.7.0" % Test exclude("joda-time", "joda-time"),
        "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2" % Test,
        "com.amazonaws" % "aws-lambda-java-log4j2" % "1.6.0" % Test,
        "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.21.1" % Test,
      )
    },
    scalacOptions += "-language:adhocExtensions", // TODO see https://github.com/disneystreaming/smithy4s/issues/1307
  )
  .dependsOn(
    `autoscaling-ecs-core`,
    `aws-testkit`,
    `feral-testkit`,
  )

lazy val `autoscaling-ecs-draining-lambda` = project
  .in(file("draining"))
  .settings(
    libraryDependencies ++= {
      Seq(
        "org.typelevel" %% "feral-lambda" % "0.2.4",
        "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
        "org.http4s" %% "http4s-ember-client" % "0.23.24",
        "org.tpolecat" %% "natchez-xray" % "0.3.4",
        "com.amazonaws" % "aws-lambda-java-log4j2" % "1.6.0" % Runtime,
        "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.22.0" % Runtime,
        "org.typelevel" %% "cats-effect-testkit" % "3.5.2" % Test,
        "org.typelevel" %% "munit-cats-effect" % "2.0.0-M4" % Test,
        "org.scalameta" %% "munit-scalacheck" % "1.0.0-M10" % Test,
        "org.typelevel" %% "scalacheck-effect-munit" % "2.0.0-M2" % Test,
        "org.typelevel" %% "log4cats-noop" % "2.6.0" % Test,
        "io.circe" %% "circe-literal" % "0.14.6" % Test,
        "io.circe" %% "circe-testing" % "0.14.6" % Test,
        "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2" % Test,
        "com.dwolla" %% "dwolla-otel-natchez" % "0.2.2" % Test,
      )
    },
    topLevelDirectory := None,
    maintainer := "devops@dwolla.com",
  )
  .dependsOn(
    `autoscaling-ecs-core`,
    `aws-testkit` % Test,
    `feral-testkit` % Test,
  )
  .enablePlugins(
    UniversalPlugin,
    JavaAppPackaging,
  )

lazy val `registrator-health-check-lambda` = project
  .in(file("registrator-health-check"))
  .settings(
    libraryDependencies ++= {
      Seq(
        "org.typelevel" %% "feral-lambda" % "0.2.4",
        "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
        "org.http4s" %% "http4s-ember-client" % "0.23.21",
        "org.typelevel" %% "mouse" % "1.2.2",
        "org.tpolecat" %% "natchez-xray" % "0.3.4",
        "com.amazonaws" % "aws-lambda-java-log4j2" % "1.6.0" % Runtime,
        "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.22.0" % Runtime,
        "org.typelevel" %% "cats-effect-testkit" % "3.5.2" % Test,
        "org.typelevel" %% "munit-cats-effect" % "2.0.0-M4" % Test,
        "org.scalameta" %% "munit-scalacheck" % "1.0.0-M10" % Test,
        "org.typelevel" %% "scalacheck-effect-munit" % "2.0.0-M2" % Test,
        "org.typelevel" %% "log4cats-noop" % "2.6.0" % Test,
        "io.circe" %% "circe-literal" % "0.14.6" % Test,
        "io.circe" %% "circe-testing" % "0.14.6" % Test,
        "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2" % Test,
      )
    },
    topLevelDirectory := None,
    maintainer := "devops@dwolla.com",
  )
  .dependsOn(
    `autoscaling-ecs-core`,
    `aws-testkit` % Test,
    `feral-testkit` % Test,
  )
  .enablePlugins(
    UniversalPlugin,
    JavaAppPackaging,
  )

lazy val `feral-testkit` = project
  .in(file("feral-testkit"))
  .settings(
    libraryDependencies ++= {
      Seq(
        "org.typelevel" %% "feral-lambda" % "0.2.4",
        "org.scalacheck" %% "scalacheck" % "1.17.0",
        "io.circe" %% "circe-testing" % "0.14.6",
      )
    },
  )

lazy val `aws-testkit` = project
  .in(file("aws-testkit"))
  .settings(
    libraryDependencies ++= {
      Seq(
        "co.fs2" %% "fs2-core" % "3.9.3",
        "org.scalacheck" %% "scalacheck" % "1.17.0",
        "com.47deg" %% "scalacheck-toolbox-datetime" % "0.7.0" exclude("joda-time", "joda-time"),
      )
    },
  )
  .dependsOn(`autoscaling-ecs-core`)

lazy val `ecs-autoscaling-lifecycle-hooks` = project
  .in(file("."))
  .aggregate(
    `autoscaling-ecs-core`,
    `autoscaling-ecs-draining-lambda`,
    `registrator-health-check-lambda`,
    `core-tests`,
    `feral-testkit`,
    `aws-testkit`,
  )
  .enablePlugins(
    ServerlessDeployPlugin,
    GitVersioning,
  )
