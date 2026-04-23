import scala.io.Source
import java.io.File
import com.github.tototoshi.csv._

import sttp.client3._

import pureconfig._
import pureconfig.generic.auto._
import scala.annotation.tailrec
import scala.io.StdIn.readLine

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

    print(responseFingrid.body)
  }

  // https://www.geeksforgeeks.org/scala/how-to-read-and-write-csv-file-in-scala/
  def readCSV(filename: String) = {

    val delimiter = ","
    val file = Source.fromFile(filename)
    for (line <- file.getLines()) {

      // Store the data somehow...

      // val fields = line.split(delimiter).map(_.trim)
      // print(fields.mkString(", "))
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

  // Idea source: https://github.com/nwtgck/loop-to-tailrec
  def mainMenu(): Unit = {

    @tailrec
    def _mainMenu(): Unit = {

      print("""
| --- MAIN MENU ---
| 1. Monitor & control energy sources
| 2. Data collection & storage
| 3. View plant status
| 4. Data analysis
| 5. Check for issues
| 0. Exit

Enter your choice:  """)

      val choice = readLine()
      if (choice != 0) {
        choice match {
          case "1" => monitorMenu()
          case "2" => dataMenu()
          case "3" => statusMenu()
          case "4" => analysisMenu()
          case "5" => issuesMenu()
          case "0" =>
            print("Exiting system...")
          case _ => {
            println("Invalid option.")
            _mainMenu()
          }
        }
      }
    }

    _mainMenu()
  }

  def monitorMenu() = {
    @tailrec
    def _monitorMenu(): Unit = {

      print("""
| --- MONITOR MENU ---
| 11. View All Energy Sources
| 12. Solar Panel Controls
| 13. Wind Turbine Controls
| 14. Hydro Power Controls
| 15. Start/Stop Energy Source
| 16. Adjust Output Levels
| 17. Back

Enter your choice:  """.stripMargin)

      val choice = readLine()
      if (choice != 0) {
        choice match {
          case "11" => viewAllResources()
          case "12" => viewSolar()
          case "13" => viewWind()
          case "14" => viewHydro()
          case "15" => startStopSource()
          case "16" => adjustOutput()
          case "17" => mainMenu()
          case "0"  =>
            print("Exiting system...")
          case _ => {
            println("Invalid option.")
            _monitorMenu()
          }
        }
      }
    }
    _monitorMenu()
  }
  def dataMenu() = {
    @tailrec
    def _dataMenu(): Unit = {

      print("""
| --- DATA ANALYSIS ---
| 21. Collect Live Data
| 22. Store Data to File
| 23. Load Data from File
| 24. Back

Enter your choice:  """.stripMargin)

      val choice = readLine()
      if (choice != 0) {
        choice match {
          case "21" => collectLiveData()
          case "22" => storeToFile()
          case "23" => loadFromFile()
          case "24" => mainMenu()
          case "0"  =>
            print("Exiting system...")
          case _ => println("Invalid option.")
        }
        _dataMenu()
      }
    }
    _dataMenu()
  }
  def statusMenu() = {
    @tailrec
    def _statusMenu(): Unit = {

      print("""
| --- PLANT STATUS ---
| 31. View Total Energy Generation
| 32. View Energy by Source
| 33. View Storage Capacity
| 34. Back

Enter your choice:  """.stripMargin)

      val choice = readLine()
      if (choice != 0) {
        choice match {
          case "31" => viewTotalGeneration()
          case "32" => viewEnergyBySource()
          case "33" => viewCapacity()
          case "34" => mainMenu()
          case "0"  =>
            print("Exiting system...")
          case _ => println("Invalid option.")
        }
        _statusMenu()
      }
    }
    _statusMenu()
  }
  def analysisMenu() = {
    @tailrec
    def _analysisMenu(): Unit = {

      print("""
| --- ANALYSIS ---
| 41. Filter Data
| 42. Search Data
| 43. Statistical Analysis
| 44. Back

Enter your choice:  """.stripMargin)

      val choice = readLine()
      if (choice != 0) {
        choice match {
          case "41" => filterMenu()
          case "42" => searchMenu()
          case "43" => statAnalysis()
          case "44" => mainMenu()
          case "0"  =>
            print("Exiting system...")
          case _ => println("Invalid option.")
        }
        _analysisMenu()
      }
    }
    _analysisMenu()
  }
  def issuesMenu() = {
    println("|--- PLANT STATUS ---\n")
    print("No issues found.")
  }

  def viewAllResources() = { println("|--- ALL RESOURCES ---\n")}
  def viewSolar() = {println("|--- SOLAR ---\n")}
  def viewWind() = { println("|--- WIND ---\n")}
  def viewHydro() = {println("|--- HYDRO ---\n")}
  def startStopSource() = { println("|--- START / STOP ---\n")}
  def adjustOutput() = {println("|--- ADJUST OUTPUT ---\n")}
  def collectLiveData() = {println("|--- COLLECT LIVE DATA ---\n")}
  def storeToFile() = {println("|--- SAVE DATA ---\n")}
  def loadFromFile() = {println("|--- LOAD DATA ---\n")}
  def viewTotalGeneration() = {println("|--- TOTAL GENERATED ENERGY ---\n")}
  def viewEnergyBySource() = { println("|--- ENERGY BY SOURCE ---\n")}
  def viewCapacity() = {println("|--- ENERGY CAPACITY ---\n")}
  def filterMenu() = {println("|--- FILTER ---\n")}
  def searchMenu() = {println("|--- SEARCH ---\n")}
  def statAnalysis() = {println("|--- STATISTICS ---\n")}

  mainMenu()

}
