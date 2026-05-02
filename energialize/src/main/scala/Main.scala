/*
Energialize
Team members:
Arlis Arto Puidet
Huba Nagy
Lilla Bagossi
*/

import pureconfig._
import pureconfig.generic.auto._

final case class AppConfig(fingridApiKey: String)
final case class Config(app: AppConfig)

object Main extends App {
  // Loading in Fingrid API key from application.conf
  private val config = ConfigSource.default.loadOrThrow[Config]
  RepApp.run(config.app.fingridApiKey)
}
