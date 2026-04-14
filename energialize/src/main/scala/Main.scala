import scala.io.Source
import java.io.File
import com.github.tototoshi.csv._

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
  
  // https://www.geeksforgeeks.org/scala/how-to-read-and-write-csv-file-in-scala/
  def readCSV(filename: String) = {

    val delimiter = ","
    val file = Source.fromFile(filename)
    for (line <- file.getLines()) {

        //Store the data somehow...

        //val fields = line.split(delimiter).map(_.trim)
        //println(fields.mkString(", "))
    }
    file.close()
  }

  // https://www.geeksforgeeks.org/scala/how-to-read-and-write-csv-file-in-scala/
  def writeToCSV(filename: String) = { 

    val writer = CSVWriter.open(new File(filename))
    val data = List(
      Map("Name" -> "John", "Age" -> "30", "Country" -> "USA"),
      Map("Name" -> "Anna", "Age" -> "28", "Country" -> "UK")
    )
    val headers = data.head.keys.toSeq
    val rows = data.map(_.values.toSeq)
    writer.writeRow(headers)
    writer.writeAll(rows)
    writer.close()
  }



  getApiData(FINGRID_API_KEY)



}