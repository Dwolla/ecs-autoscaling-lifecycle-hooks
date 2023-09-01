ThisBuild / organization := "Dwolla"
ThisBuild / homepage := Option(url("https://github.com/Dwolla/autoscaling-ecs-draining-lambda"))
ThisBuild / tlCiDependencyGraphJob := false
ThisBuild / scalaVersion := "3.3.0"
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

lazy val `autoscaling-ecs-core`: Project = project
  .in(file("core"))
  .settings(
    libraryDependencies ++= {
      Seq(
        "com.dwolla" %% "fs2-utils" % "3.0.0-RC2",
        "org.typelevel" %% "feral-lambda" % "0.2.3",
        "org.typelevel" %% "log4cats-core" % "2.6.0",
        "io.circe" %% "circe-parser" % "0.14.5",
        "io.monix" %% "newtypes-core" % "0.2.3",
        "io.monix" %% "newtypes-circe-v0-14" % "0.2.3",

        // TODO when smithy4s is updated, hopefully these Java SDK artifacts can be replaced with smithy4s equivalents
        "software.amazon.awssdk" % "autoscaling" % "2.20.139",
        "software.amazon.awssdk" % "sns" % "2.20.139",
        "software.amazon.awssdk" % "ec2" % "2.20.139",
        "software.amazon.awssdk" % "cloudformation" % "2.20.139",
      )
    }
  )
  .dependsOn(
    `smithy4s-generated`,
  )

lazy val `core-tests` = project
  .in(file("core-tests"))
  .settings(
    libraryDependencies ++= {
      Seq(
        "org.typelevel" %% "cats-effect-testkit" % "3.5.1" % Test,
        "org.typelevel" %% "munit-cats-effect" % "2.0.0-M3" % Test,
        "org.scalameta" %% "munit-scalacheck" % "1.0.0-M8" % Test,
        "org.typelevel" %% "scalacheck-effect-munit" % "2.0.0-M2" % Test,
        "org.typelevel" %% "log4cats-noop" % "2.6.0" % Test,
        "org.typelevel" %% "mouse" % "1.2.1" % Test,
        "io.circe" %% "circe-literal" % "0.14.5" % Test,
        "io.circe" %% "circe-testing" % "0.14.5" % Test,
        "com.47deg" %% "scalacheck-toolbox-datetime" % "0.7.0" % Test exclude("joda-time", "joda-time"),
        "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2" % Test,
      )
    }
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
        "org.typelevel" %% "feral-lambda" % "0.2.3",
        "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
        "org.http4s" %% "http4s-ember-client" % "0.23.23",
        "com.amazonaws" % "aws-lambda-java-log4j2" % "1.5.1" % Runtime,
        "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.20.0" % Runtime,
        "org.typelevel" %% "cats-effect-testkit" % "3.5.1" % Test,
        "org.typelevel" %% "munit-cats-effect" % "2.0.0-M3" % Test,
        "org.scalameta" %% "munit-scalacheck" % "1.0.0-M8" % Test,
        "org.typelevel" %% "scalacheck-effect-munit" % "2.0.0-M2" % Test,
        "org.typelevel" %% "log4cats-noop" % "2.6.0" % Test,
        "io.circe" %% "circe-literal" % "0.14.5" % Test,
        "io.circe" %% "circe-testing" % "0.14.5" % Test,
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

lazy val `registrator-health-check-lambda` = project
  .in(file("registrator-health-check"))
  .settings(
    libraryDependencies ++= {
      Seq(
        "org.typelevel" %% "feral-lambda" % "0.2.3",
        "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
        "org.http4s" %% "http4s-ember-client" % "0.23.21",
        "org.typelevel" %% "mouse" % "1.2.1",
        "com.amazonaws" % "aws-lambda-java-log4j2" % "1.5.1" % Runtime,
        "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.20.0" % Runtime,
        "org.typelevel" %% "cats-effect-testkit" % "3.5.1" % Test,
        "org.typelevel" %% "munit-cats-effect" % "2.0.0-M3" % Test,
        "org.scalameta" %% "munit-scalacheck" % "1.0.0-M8" % Test,
        "org.typelevel" %% "scalacheck-effect-munit" % "2.0.0-M2" % Test,
        "org.typelevel" %% "log4cats-noop" % "2.6.0" % Test,
        "io.circe" %% "circe-literal" % "0.14.5" % Test,
        "io.circe" %% "circe-testing" % "0.14.5" % Test,
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
        "org.typelevel" %% "feral-lambda" % "0.2.3",
        "org.scalacheck" %% "scalacheck" % "1.17.0",
        "io.circe" %% "circe-testing" % "0.14.5",
      )
    },
  )

lazy val `aws-testkit` = project
  .in(file("aws-testkit"))
  .settings(
    libraryDependencies ++= {
      Seq(
        "co.fs2" %% "fs2-core" % "3.9.0",
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
