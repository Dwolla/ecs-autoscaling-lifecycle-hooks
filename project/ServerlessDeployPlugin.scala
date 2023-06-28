import com.typesafe.sbt.packager.universal.UniversalPlugin
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.*
import sbt.Keys.*
import sbt.internal.util.complete.DefaultParsers.*
import sbt.internal.util.complete.Parser
import sbt.{Def, settingKey, IO as _, *}

object ServerlessDeployPlugin extends AutoPlugin {
  object autoImport {
    val serverlessDeployCommand = settingKey[Seq[String]]("serverless command to deploy the application")
    val deploy = inputKey[Int]("deploy to AWS")
  }

  import autoImport.*

  override def trigger: PluginTrigger = NoTrigger

  override def requires: Plugins = UniversalPlugin

  override lazy val projectSettings: Seq[Setting[?]] = Seq(
    serverlessDeployCommand := "serverless deploy --verbose".split(' ').toSeq,
    deploy := Def.inputTask {
      import scala.sys.process.*

      val baseCommand = serverlessDeployCommand.value
      val exitCode = Process(
        baseCommand ++ Seq("--stage", Stage.parser.parsed.name),
        Option((ThisBuild / baseDirectory).value),
        "ARTIFACT_PATH" -> (Universal / packageBin).value.toString,
        "VERSION" -> version.value,
        "VCS_URL" -> (ThisBuild / homepage).value.get.toString,
      ).!

      if (exitCode == 0) exitCode
      else throw new IllegalStateException("Serverless returned a non-zero exit code. Please check the logs for more information.")
    }.evaluated
  )

  sealed abstract class Stage(val name: String) {
    val parser: Parser[this.type] = (Space ~> token(this.toString)).map(_ => this)
  }

  object Stage {
    val parser: Parser[Stage] =
        token(Stage.Admin.parser) |
        token(Stage.Sandbox.parser)

    case object Admin extends Stage("admin")
    case object Sandbox extends Stage("sandbox")
  }
}
