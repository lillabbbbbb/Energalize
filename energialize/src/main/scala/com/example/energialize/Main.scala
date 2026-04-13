import sttp.client3.*

object Main extends App{
  def getApiData(String: apiKey): Unit = {
    val backend = HttpURLConnectionBackend()

    val requestFingrid = basicRequest
      .get(uri"https://data.fingrid.fi/api/datasets/247")
      .header("x-api-key", apiKey)

    val responseFingrid = requestFingrid.send(backend)

    println(responseFingrid.body)
    return responseFingrid.body

  }

  def main(args: Array[String]): Unit = {
      getApiData("5808a2f1f3dd4776a7e775cdd4e0bf45")
  }
}
