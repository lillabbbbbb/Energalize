import sttp.client3._

import pureconfig._
import pureconfig.generic.auto._

case class AppConfig(fingridApiKey: String)
case class Config(app: AppConfig)

object Main extends App {
  private val config = ConfigSource.default.loadOrThrow[Config]
  private val FINGRID_API_KEY: String = config.app.fingridApiKey

  def getApiData(apiKey: String): Unit = {
    val backend = HttpURLConnectionBackend()

    val requestFingrid = basicRequest
      .get(uri"https://data.fingrid.fi/api/datasets/247")
      .header("x-api-key", apiKey)

    val responseFingrid = requestFingrid.send(backend)

    println(responseFingrid.body)
  }
  getApiData(FINGRID_API_KEY)
}