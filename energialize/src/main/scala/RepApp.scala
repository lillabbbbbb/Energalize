// CLI app: menus and interactive operations for plant management

import java.io.File
import scala.annotation.tailrec
import scala.io.StdIn.readLine
import scala.util.Try

import com.github.tototoshi.csv._

import application.PlantUseCases
import api.EnergyDataProvider
import functions.DataAnalysis
import model.{Alert, AppState, EnergyReading, PlantAsset}

// Holds dependencies used across the interactive CLI
final case class AppContext(
  plantUseCases: PlantUseCases,
  dataProvider: EnergyDataProvider
)

object RepApp {
  def run(apiKey: String): Unit = {
    // Set up initial context
    val plantUseCases = PlantServices
    val dataProvider = FingridEnergyDataProvider(apiKey)
    val context = AppContext(plantUseCases, dataProvider)
    // Load in previously saved state if exist
    val initialState = RepIO.loadSystemState(RepConstants.DefaultAssets, plantUseCases)
    mainMenu(context, initialState)
  }

  private def mainMenu(context: AppContext, state: AppState): Unit = {
    @tailrec
    def loop(current: AppState): Unit = {
      print("""
| --- MAIN MENU ---
| 1. Monitor & control energy sources
| 2. Data collection & storage
| 3. View plant status
| 4. Data analysis
| 5. Check for issues
| 0. Exit

Enter your choice:  """)

      readLine() match {
        case "1" => loop(monitorMenu(context, current))
        case "2" => loop(dataMenu(context, current))
        case "3" => loop(statusMenu(context, current))
        case "4" => loop(analysisMenu(context, current))
        case "5" => loop(issuesMenu(context, current))
        case "0" => exitProgram(current)
        case _ =>
          println("Invalid option.")
          loop(current)
      }
    }

    loop(state)
  }

  private def monitorMenu(context: AppContext, state: AppState): AppState = {
    @tailrec
    def loop(current: AppState): AppState = {
      print("""
| --- MONITOR MENU ---
| 11. View All Energy Sources
| 12. Solar Panel Controls
| 13. Wind Turbine Controls
| 14. Hydro Power Controls
| 15. Start/Stop Energy Source
| 16. Adjust Output Levels
| 17. Add Power Generator
| 18. Save Generators to File
| 19. Load Generators from File
| 20. Back

Enter your choice:  """.stripMargin)

      readLine() match {
        case "11" =>
          viewAllResources(context, current)
          loop(current)
        case "12" =>
          loop(viewSolar(current))
        case "13" =>
          loop(viewWind(current))
        case "14" =>
          loop(viewHydro(current))
        case "15" =>
          loop(startStopSource(current))
        case "16" =>
          loop(adjustOutput(current))
        case "17" =>
          loop(addGenerator(current))
        case "18" =>
          storeGeneratorsToFile(current)
          loop(current)
        case "19" =>
          loop(loadGeneratorsFromFile(context, current))
        case "20" => current
        case "0" => exitProgram(current)
        case _ =>
          println("Invalid option.")
          loop(current)
      }
    }

    loop(state)
  }

  private def dataMenu(context: AppContext, state: AppState): AppState = {
    @tailrec
    def loop(current: AppState): AppState = {
      print("""
| --- DATA COLLECTION ---
| 21. Collect Live Data
| 22. Store Data to File
| 23. Load Data from File
| 24. Back

Enter your choice:  """.stripMargin)

      readLine() match {
        case "21" =>
          val nextState = collectLiveData(context, current)
          loop(nextState)
        case "22" =>
          storeToFile(current)
          loop(current)
        case "23" =>
          val nextState = loadFromFile(context, current)
          loop(nextState)
        case "24" => current
        case "0" => exitProgram(current)
        case _ =>
          println("Invalid option.")
          loop(current)
      }
    }

    loop(state)
  }

  private def statusMenu(context: AppContext, state: AppState): AppState = {
    @tailrec
    def loop(current: AppState): AppState = {
      print("""
| --- PLANT STATUS ---
| 31. View Total Energy Generation
| 32. View Energy by Source
| 33. View Storage Capacity
| 34. View Generation Data
| 35. Back

Enter your choice:  """.stripMargin)

      readLine() match {
        case "31" =>
          viewTotalGeneration(current)
          loop(current)
        case "32" =>
          viewEnergyBySource(current)
          loop(current)
        case "33" =>
          viewCapacity(current)
          loop(current)
        case "34" =>
          displayGenerationData(current)
          loop(current)
        case "35" => current
        case "0" => exitProgram(current)
        case _ =>
          println("Invalid option.")
          loop(current)
      }
    }

    loop(state)
  }

  private def analysisMenu(context: AppContext, state: AppState): AppState = {
    @tailrec
    def loop(current: AppState): AppState = {
      print("""
| --- ANALYSIS ---
| 41. Filter Data
| 42. Search Data
| 43. Statistical Analysis
| 44. Generation vs Consumption Analytics
| 45. Back

Enter your choice:  """.stripMargin)

      readLine() match {
        case "41" =>
          filterMenu(current)
          loop(current)
        case "42" =>
          searchMenu(current)
          loop(current)
        case "43" =>
          statAnalysis(context, current)
          loop(current)
        case "44" =>
          generationConsumptionAnalytics(context, current)
          loop(current)
        case "45" => current
        case "0" => exitProgram(current)
        case _ =>
          println("Invalid option.")
          loop(current)
      }
    }

    loop(state)
  }

  private def issuesMenu(context: AppContext, state: AppState): AppState = {
    println("|--- PLANT STATUS ---\n")
    val generatedAlerts = context.plantUseCases.alerts(state.assets, state.readings)
    // also include some randomly generated alerts
    val randomAlerts = RepLogic.generateRandomAlerts(state.assets)
    val mergedAlerts = (state.alerts ++ generatedAlerts ++ randomAlerts).distinct

    val updatedAlerts = dismissAlertsLoop(mergedAlerts)
    state.copy(alerts = updatedAlerts)
  }

  // Print overview and list all assets
  private def viewAllResources(context: AppContext, state: AppState): Unit = {
    println("|--- ALL RESOURCES ---\n")
    println(context.plantUseCases.overview(state.assets, state.readings))
    state.assets.foreach(asset => println(RepFormatting.formatAsset(asset)))
  }

  // Show solar assets and optionally adjust one
  private def viewSolar(state: AppState): AppState = {
    println("|--- SOLAR ---\n")
    val solarAssets = state.assets.filter(_.source == model.EnergySource.Solar)
    solarAssets.foreach(asset => println(RepFormatting.formatAsset(asset)))
    println("Enter solar generator id to adjust output, or press Enter to go back: ")
    val targetId = readLine().trim
    if (targetId.isEmpty) {
      state
    } else {
      adjustOutputForAsset(state, targetId)
    }
  }

  // Show wind assets and allow toggling
  private def viewWind(state: AppState): AppState = {
    println("|--- WIND ---\n")
    val windAssets = state.assets.filter(_.source == model.EnergySource.Wind)
    windAssets.foreach(asset => println(RepFormatting.formatAsset(asset)))
    println("Enter wind generator id to toggle, or press Enter to go back: ")
    val targetId = readLine().trim
    if (targetId.isEmpty) {
      state
    } else {
      toggleGenerator(state, targetId)
    }
  }

  // Show hydro assets and allow toggling
  private def viewHydro(state: AppState): AppState = {
    println("|--- HYDRO ---\n")
    val hydroAssets = state.assets.filter(_.source == model.EnergySource.Hydro)
    hydroAssets.foreach(asset => println(RepFormatting.formatAsset(asset)))
    println("Enter hydro generator id to toggle, or press Enter to go back: ")
    val targetId = readLine().trim
    if (targetId.isEmpty) {
      state
    } else {
      toggleGenerator(state, targetId)
    }
  }

  // Prompt and toggle an asset state
  private def startStopSource(state: AppState): AppState = {
    println("|--- START / STOP ---\n")
    println("Enter asset id to toggle: ")
    val assetId = readLine()
    toggleGenerator(state, assetId)
  }

  // Prompt and adjust an asset's output factor
  private def adjustOutput(state: AppState): AppState = {
    println("|--- ADJUST OUTPUT ---\n")
    println("Enter asset id to adjust: ")
    val assetId = readLine()
    adjustOutputForAsset(state, assetId)
  }

  // Fetch live demand readings and synthesize generation readings
  private def collectLiveData(context: AppContext, state: AppState): AppState = {
    println("|--- COLLECT LIVE DATA ---\n")
    context.dataProvider.fetchReadings() match {
      case Left(error) =>
        println(error.message)
        state
      case Right(demandReadings) =>
        val generated = demandReadings.flatMap(reading => RepLogic.generateForDemand(state.assets, reading))
        val merged = state.readings ++ demandReadings ++ generated
        println(
          s"Collected ${demandReadings.size} demand points and generated ${generated.size} readings."
        )
        state.copy(readings = merged)
    }
  }

  // Save current readings to a CSV at user-specified path
  private def storeToFile(state: AppState): Unit = {
    println("|--- SAVE DATA ---\n")
    println("Enter CSV path to write: ")
    val path = readLine()
    if (state.readings.isEmpty) {
      println("No readings to store.")
    } else {
      val writer = CSVWriter.open(new File(path))
      val headers = List(
        "assetId",
        "timestamp",
        "source",
        "energyKwh",
        "storageCapacityKwh",
        "healthStatus",
        "notes"
      )
      writer.writeRow(headers)
      state.readings.foreach { reading =>
        writer.writeRow(
          List(
            reading.assetId,
            reading.timestamp.format(RepConstants.TimestampFormatter),
            RepFormatting.formatSource(reading.source),
            reading.energyKwh.toString,
            reading.storageCapacityKwh.map(_.toString).getOrElse(""),
            RepFormatting.formatHealthStatus(reading.healthStatus),
            reading.notes.getOrElse("")
          )
        )
      }
      writer.close()
      println(s"Stored ${state.readings.size} readings to $path")
    }
  }

  // Load readings from CSV via PlantUseCases
  private def loadFromFile(context: AppContext, state: AppState): AppState = {
    println("|--- LOAD DATA ---\n")
    println("Enter CSV path to read: ")
    val path = readLine()
    context.plantUseCases.loadReadings(path) match {
      case Left(error) =>
        println(error.message)
        state
      case Right(readings) =>
        println(s"Loaded ${readings.size} readings.")
        state.copy(readings = readings)
    }
  }

  // Print total generated energy excluding demand
  private def viewTotalGeneration(state: AppState): Unit = {
    println("|--- TOTAL GENERATED ENERGY ---\n")
    val total = state.readings.filterNot(RepLogic.isDemandReading).map(_.energyKwh).sum
    println(s"Total energy generated: ${RepFormatting.formatDouble(total)} kWh")
  }

  // Print generation totals grouped by source
  private def viewEnergyBySource(state: AppState): Unit = {
    println("|--- ENERGY BY SOURCE ---\n")
    val grouped = state.readings
      .filterNot(RepLogic.isDemandReading)
      .groupBy(_.source)
      .view
      .mapValues(_.map(_.energyKwh).sum)
      .toMap
    val sources = List(model.EnergySource.Solar, model.EnergySource.Wind, model.EnergySource.Hydro)
    sources.foreach { source =>
      val total = grouped.getOrElse(source, 0.0)
      println(s"${RepFormatting.formatSource(source)}: ${RepFormatting.formatDouble(total)} kWh")
    }
  }

  // Print total storage capacity across readings
  private def viewCapacity(state: AppState): Unit = {
    println("|--- ENERGY CAPACITY ---\n")
    val capacity = state.readings.flatMap(_.storageCapacityKwh).sum
    println(s"Total storage capacity: ${RepFormatting.formatDouble(capacity)} kWh")
  }

  // Display recent generation and demand readings
  private def displayGenerationData(state: AppState): Unit = {
    println("|--- GENERATION DATA ---\n")
    val generationReadings = state.readings.filterNot(RepLogic.isDemandReading).sortBy(_.timestamp).takeRight(10)
    val demandReadings = state.readings.filter(RepLogic.isDemandReading).sortBy(_.timestamp).takeRight(10)
    println("Latest generation readings:")
    if (generationReadings.isEmpty) {
      println("No generation data available.")
    } else {
      generationReadings.foreach(reading => println(RepFormatting.formatReading(reading)))
    }
    println("\nLatest demand readings:")
    if (demandReadings.isEmpty) {
      println("No demand data available.")
    } else {
      demandReadings.foreach(reading => println(RepFormatting.formatReading(reading)))
    }
  }

  // Filter readings by granularity and date using DataAnalysis helpers
  private def filterMenu(state: AppState): Unit = {
    println("|--- FILTER ---\n")
    println("Choose granularity: 1) Hourly 2) Daily 3) Weekly 4) Monthly")
    val granularity = readLine() match {
      case "1" => Some(model.TimeGranularity.Hourly)
      case "2" => Some(model.TimeGranularity.Daily)
      case "3" => Some(model.TimeGranularity.Weekly)
      case "4" => Some(model.TimeGranularity.Monthly)
      case _ => None
    }
    granularity match {
      case None => println("Invalid granularity.")
      case Some(gran) =>
        gran match {
          case model.TimeGranularity.Hourly =>
            println("Enter date/time (DD/MM/YYYY HH or DD/MM/YYYY HH:mm), e.g., 01/01/2026 13 or 01/01/2026 13:30")
          case model.TimeGranularity.Daily =>
            println("Enter date (DD/MM/YYYY), e.g., 01/01/2026")
          case model.TimeGranularity.Weekly =>
            println("Enter date (DD/MM/YYYY) within the week, e.g., 01/01/2026")
          case model.TimeGranularity.Monthly =>
            println("Enter month (YYYY-MM or MM/YYYY), e.g., 2026-01 or 01/2026")
        }
        val dateInput = readLine()
        println("Sort order: 1) Ascending 2) Descending")
        val ascending = readLine() match {
          case "2" => false
          case _ => true
        }
        val result = gran match {
          case model.TimeGranularity.Hourly => DataAnalysis.sortHourly(state.readings, dateInput, ascending)
          case model.TimeGranularity.Daily => DataAnalysis.sortDaily(state.readings, dateInput, ascending)
          case model.TimeGranularity.Weekly => DataAnalysis.sortWeekly(state.readings, dateInput, ascending)
          case model.TimeGranularity.Monthly => DataAnalysis.sortMonthly(state.readings, dateInput, ascending)
        }
        result match {
          case Left(error) => println(error.message)
          case Right(filtered) => filtered.foreach(reading => println(RepFormatting.formatReading(reading)))
        }
    }
  }

  // Search helper menu by asset id, source or time windows
  private def searchMenu(state: AppState): Unit = {
    println("|--- SEARCH ---\n")
    println("Search by: 1) Asset Id 2) Source 3) Hourly 4) Daily 5) Weekly 6) Monthly")
    readLine() match {
      case "1" =>
        println("Enter asset id: ")
        val assetId = readLine()
        val results = state.readings.filter(_.assetId == assetId)
        printSearchResults(results, s"Asset $assetId")
      case "2" =>
        println("Enter source (Solar/Wind/Hydro): ")
        val source = RepParsing.parseEnergySource(readLine())
        val results = state.readings.filter(_.source == source)
        printSearchResults(results, s"Source ${RepFormatting.formatSource(source)}")
      case "3" =>
        println("Enter date/time (DD/MM/YYYY HH or DD/MM/YYYY HH:mm): ")
        val input = readLine()
        DataAnalysis.sortHourly(state.readings, input) match {
          case Left(error) => println(error.message)
          case Right(results) => printSearchResults(results, s"Hourly $input")
        }
      case "4" =>
        println("Enter date (DD/MM/YYYY): ")
        val input = readLine()
        DataAnalysis.sortDaily(state.readings, input) match {
          case Left(error) => println(error.message)
          case Right(results) => printSearchResults(results, s"Daily $input")
        }
      case "5" =>
        println("Enter date (DD/MM/YYYY): ")
        val input = readLine()
        DataAnalysis.sortWeekly(state.readings, input) match {
          case Left(error) => println(error.message)
          case Right(results) => printSearchResults(results, s"Weekly $input")
        }
      case "6" =>
        println("Enter month (YYYY-MM or MM/YYYY): ")
        val input = readLine()
        DataAnalysis.sortMonthly(state.readings, input) match {
          case Left(error) => println(error.message)
          case Right(results) => printSearchResults(results, s"Monthly $input")
        }
      case _ => println("Invalid option.")
    }
  }

  // Compute and display basic statistics via use cases
  private def statAnalysis(context: AppContext, state: AppState): Unit = {
    println("|--- STATISTICS ---\n")
    context.plantUseCases.stats(state.readings) match {
      case Left(error) => println(error.message)
      case Right(summary) => println(RepFormatting.formatStats(summary))
    }
  }

  private def printSearchResults(readings: List[EnergyReading], criteria: String): Unit = {
    if (readings.isEmpty) {
      println(s"No available data for $criteria. Please choose another input.")
    } else {
      println(s"Results for $criteria:")
      readings.sortBy(_.timestamp).foreach(reading => println(RepFormatting.formatReading(reading)))
    }
  }

  // Toggle asset enabled state and return updated AppState
  private def toggleGenerator(state: AppState, assetId: String): AppState = {
    val updated = state.assets.map { asset =>
      if (asset.id == assetId) asset.copy(isEnabled = !asset.isEnabled) else asset
    }
    if (updated == state.assets) {
      println("Unknown asset id.")
      state
    } else {
      val status = if (updated.find(_.id == assetId).exists(_.isEnabled)) "enabled" else "disabled"
      println(s"$assetId is now $status.")
      state.copy(assets = updated)
    }
  }

  // Adjust an asset's output factor by percentage
  private def adjustOutputForAsset(state: AppState, assetId: String): AppState = {
    state.assets.find(_.id == assetId) match {
      case None =>
        println("Unknown asset id.")
        state
      case Some(_) =>
        println("Enter new output level (%): ")
        val percentage = readLine()
        Try(percentage.toDouble).toOption match {
          case Some(value) if value >= 0 =>
            val factor = value / 100.0
            val updated = state.assets.map { asset =>
              if (asset.id == assetId) asset.copy(outputFactor = factor) else asset
            }
            println(s"Adjusted output of $assetId to ${RepFormatting.formatDouble(value)}%.")
            state.copy(assets = updated)
          case _ =>
            println("Invalid percentage.")
            state
        }
    }
  }

  // Compare generation vs consumption and show stats for each
  private def generationConsumptionAnalytics(context: AppContext, state: AppState): Unit = {
    println("|--- GENERATION VS CONSUMPTION ---\n")
    val demandReadings = state.readings.filter(RepLogic.isDemandReading)
    val generationReadings = state.readings.filterNot(RepLogic.isDemandReading)
    val demandTotal = demandReadings.map(_.energyKwh).sum
    val generationTotal = generationReadings.map(_.energyKwh).sum
    println(s"Total demand: ${RepFormatting.formatDouble(demandTotal)} kWh")
    println(s"Total generation: ${RepFormatting.formatDouble(generationTotal)} kWh")
    println(s"Balance: ${RepFormatting.formatDouble(generationTotal - demandTotal)} kWh")
    println("\nDemand statistics:")
    context.plantUseCases.stats(demandReadings) match {
      case Left(error) => println(error.message)
      case Right(summary) => println(RepFormatting.formatStats(summary))
    }
    println("\nGeneration statistics:")
    context.plantUseCases.stats(generationReadings) match {
      case Left(error) => println(error.message)
      case Right(summary) => println(RepFormatting.formatStats(summary))
    }
  }

  // Save generator metadata to CSV
  private def storeGeneratorsToFile(state: AppState): Unit = {
    println("|--- SAVE GENERATORS ---\n")
    println("Enter CSV path to write: ")
    val path = readLine()
    if (state.assets.isEmpty) {
      println("No generators to store.")
    } else {
      RepIO.saveGeneratorsState(state.assets, path)
      println(s"Stored ${state.assets.size} generators to $path")
    }
  }

  // Load generator metadata from CSV
  private def loadGeneratorsFromFile(context: AppContext, state: AppState): AppState = {
    println("|--- LOAD GENERATORS ---\n")
    println("Enter CSV path to read: ")
    val path = readLine()
    context.plantUseCases.loadAssets(path) match {
      case Left(error) =>
        println(error.message)
        state
      case Right(assets) =>
        println(s"Loaded ${assets.size} generators.")
        state.copy(assets = assets)
    }
  }

  // Prompt user and add a new PlantAsset to state
  private def addGenerator(state: AppState): AppState = {
    println("|--- ADD POWER GENERATOR ---\n")
    println("Enter generator id: ")
    val id = readLine()
    if (id.trim.isEmpty || state.assets.exists(_.id == id)) {
      println("Invalid or duplicate generator id.")
      state
    } else {
      println("Enter name: ")
      val name = readLine()
      println("Enter source (Solar/Wind/Hydro): ")
      val source = RepParsing.parseEnergySource(readLine())
      println("Enter location: ")
      val location = readLine()
      println("Enter rated capacity (kW): ")
      val capacityInput = readLine()
      Try(capacityInput.toDouble).toOption match {
        case Some(capacityKw) if capacityKw > 0 =>
          val asset = PlantAsset(id.trim, name.trim, source, location.trim, capacityKw, isEnabled = true, outputFactor = 1.0)
          println(s"Added generator $id with ${RepFormatting.formatDouble(capacityKw)} kW capacity.")
          state.copy(assets = state.assets :+ asset)
        case _ =>
          println("Invalid capacity.")
          state
      }
    }
  }

  // Loop menu to dismiss alerts one by one
  @tailrec
  private def dismissAlertsLoop(alerts: List[Alert]): List[Alert] = {
    if (alerts.isEmpty) {
      println("No issues found.")
      alerts
    } else {
      alerts.zipWithIndex.foreach { case (alert, index) =>
        println(s"${index + 1}. ${RepFormatting.formatAlert(alert)}")
      }
      println("Enter alert number to dismiss, or press Enter to go back: ")
      val input = readLine().trim
      if (input.isEmpty) {
        alerts
      } else {
        Try(input.toInt).toOption match {
          case Some(index) if index >= 1 && index <= alerts.size =>
            println("Alert dismissed.")
            val remaining = alerts.patch(index - 1, Nil, 1)
            dismissAlertsLoop(remaining)
          case _ =>
            println("Invalid selection.")
            dismissAlertsLoop(alerts)
        }
      }
    }
  }

  // Persist state and exit
  private def exitProgram(state: AppState): AppState = {
    RepIO.saveSystemState(state)
    println("Exiting system...")
    System.exit(0)
    state
  }
}
