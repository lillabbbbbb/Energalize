import pureconfig._
import pureconfig.generic.auto._

final case class AppConfig(fingridApiKey: String)
final case class Config(app: AppConfig)

object Main extends App {
  private val config = ConfigSource.default.loadOrThrow[Config]
  RepApp.run(config.app.fingridApiKey)
}
