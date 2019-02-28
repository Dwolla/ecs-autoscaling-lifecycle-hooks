javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

lazy val commonSettings = Seq(
  organization := "Dwolla",
  homepage := Option(url("https://github.com/Dwolla/autoscaling-ecs-draining-lambda")),
)

lazy val specs2Version = "4.4.1"
lazy val awsSdkVersion = "1.11.490"

lazy val root = (project in file("."))
  .settings(
    name := "autoscaling-ecs-draining-lambda",
    resolvers ++= Seq(
      Resolver.bintrayRepo("dwolla", "maven")
    ),
    libraryDependencies ++= {
      val circeVersion = "0.11.1"
      val log4j2Version = "2.11.2"
      Seq(
        "com.dwolla" %% "fs2-aws" % "2.0.0-M3",
        "com.amazonaws" % "aws-java-sdk-ecs" % awsSdkVersion,
        "com.amazonaws" % "aws-java-sdk-autoscaling" % awsSdkVersion,
        "com.amazonaws" % "aws-java-sdk-sns" % awsSdkVersion,
        "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
        "com.amazonaws" % "aws-lambda-java-log4j2" % "1.0.0",
        "com.amazonaws" % "aws-lambda-java-events" % "2.2.5",
        "org.apache.logging.log4j" % "log4j-api" % log4j2Version,
        "org.apache.logging.log4j" % "log4j-core" % log4j2Version,
        "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4j2Version,
        "io.circe" %% "circe-fs2" % "0.11.0",
        "io.circe" %% "circe-optics" % "0.11.0",
        "io.circe" %% "circe-generic-extras" % circeVersion,
        "io.chrisdavenport" %% "log4cats-slf4j" % "0.3.0",
      ) ++ Seq(
        "org.specs2" %% "specs2-core" % specs2Version,
        "org.specs2" %% "specs2-mock" % specs2Version,
        "org.specs2" %% "specs2-matcher-extra" % specs2Version,
        "org.specs2" %% "specs2-cats" % specs2Version,
        "org.specs2" %% "specs2-scalacheck" % specs2Version,
        "com.dwolla" %% "scala-aws-utils-testkit" % "1.6.1",
        "org.scalacheck" %% "scalacheck" % "1.14.0",
        "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % "1.2.0",
        "io.chrisdavenport" %% "cats-scalacheck" % "0.1.0",
        "io.circe" %% "circe-literal" % circeVersion,
        "com.47deg" %% "scalacheck-toolbox-datetime" % "0.2.5",
        "org.typelevel" %% "cats-effect-laws" % "1.2.0",
      ).map(_ % Test)
    },
  )
  .settings(commonSettings: _*)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
  .enablePlugins(PublishToS3)

lazy val stack: Project = (project in file("stack"))
  .settings(commonSettings: _*)
  .settings(
    resolvers ++= Seq(Resolver.jcenterRepo),
    libraryDependencies ++= {
      val awscdkVersion = "0.24.1"
      Seq(
        "software.amazon.awscdk" % "ecs" % awscdkVersion,
        "software.amazon.awscdk" % "sns" % awscdkVersion,
        "software.amazon.awscdk" % "lambda" % awscdkVersion,
        "software.amazon.awscdk" % "autoscaling" % awscdkVersion,
        "org.typelevel" %% "cats-effect" % "1.2.0",
        "io.circe" %% "circe-optics" % "0.11.0",
        "co.fs2" %% "fs2-io" % "1.0.3",
      )
    },
    stackName := (name in root).value,
    stackParameters := List(
      "S3Bucket" → (s3Bucket in root).value,
      "S3Key" → (s3Key in root).value
    ),
    awsAccountId := sys.props.get("AWS_ACCOUNT_ID"),
    awsRoleName := Option("cloudformation/deployer/cloudformation-deployer"),
  )
  .enablePlugins(CloudFormationStack)
  .dependsOn(root)

assemblyMergeStrategy in assembly := {
  case PathList(ps @ _*) if ps.last == "Log4j2Plugins.dat" => sbtassembly.Log4j2MergeStrategy.plugincache
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case PathList("log4j2.xml") => MergeStrategy.singleOrError
  case _ ⇒ MergeStrategy.first
}
test in assembly := {}
