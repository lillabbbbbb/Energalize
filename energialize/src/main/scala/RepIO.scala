import java.io.File
import java.time.LocalDateTime
import scala.util.Try

import com.github.tototoshi.csv._

import application.PlantUseCases
import model.{Alert, AppState, EnergyReading, PlantAsset}

object RepIO {
  def loadSystemState(defaultAssets: List[PlantAsset], plantUseCases: PlantUseCases): AppState = {
    val assets = loadAssetsIfExists(RepConstants.GeneratorsStatePath, plantUseCases).getOrElse(defaultAssets)
    val readings = loadReadingsIfExists(RepConstants.ReadingsStatePath, plantUseCases).getOrElse(List.empty)
    val alerts = loadAlertsIfExists(RepConstants.AlertsStatePath).getOrElse(List.empty)
    AppState(assets = assets, readings = readings, alerts = alerts)
  }

  def saveSystemState(state: AppState): Unit = {
    saveGeneratorsState(state.assets, RepConstants.GeneratorsStatePath)
    saveReadingsState(state.readings, RepConstants.ReadingsStatePath)
    saveAlertsState(state.alerts, RepConstants.AlertsStatePath)
    println("System state saved.")
  }

  def saveGeneratorsState(assets: List[PlantAsset], path: String): Unit = {
    val writer = CSVWriter.open(new File(path))
    val headers = List("id", "name", "source", "location", "ratedCapacityKw", "isEnabled", "outputFactor")
    writer.writeRow(headers)
    assets.foreach { asset =>
      writer.writeRow(
        List(
          asset.id,
          asset.name,
          RepFormatting.formatSource(asset.source),
          asset.location,
          asset.ratedCapacityKw.toString,
          asset.isEnabled.toString,
          asset.outputFactor.toString
        )
      )
    }
    writer.close()
  }

  def saveReadingsState(readings: List[EnergyReading], path: String): Unit = {
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
    readings.foreach { reading =>
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
  }

  def saveAlertsState(alerts: List[Alert], path: String): Unit = {
    val writer = CSVWriter.open(new File(path))
    val headers = List("assetId", "timestamp", "severity", "message")
    writer.writeRow(headers)
    alerts.foreach { alert =>
      writer.writeRow(
        List(
          alert.assetId,
          alert.timestamp.format(RepConstants.TimestampFormatter),
          alert.severity,
          alert.message
        )
      )
    }
    writer.close()
  }

  private def loadAssetsIfExists(path: String, plantUseCases: PlantUseCases): Option[List[PlantAsset]] = {
    if (!new File(path).exists()) {
      None
    } else {
      plantUseCases.loadAssets(path).toOption
    }
  }

  private def loadReadingsIfExists(path: String, plantUseCases: PlantUseCases): Option[List[EnergyReading]] = {
    if (!new File(path).exists()) {
      None
    } else {
      plantUseCases.loadReadings(path).toOption
    }
  }

  private def loadAlertsIfExists(path: String): Option[List[Alert]] = {
    if (!new File(path).exists()) {
      None
    } else {
      val result = Try {
        val reader = CSVReader.open(new File(path))
        val rows = reader.allWithHeaders()
        reader.close()
        rows.map { row =>
          val timestamp = LocalDateTime.parse(row.getOrElse("timestamp", ""), RepConstants.TimestampFormatter)
          Alert(
            assetId = row.getOrElse("assetId", ""),
            timestamp = timestamp,
            severity = row.getOrElse("severity", "UNKNOWN"),
            message = row.getOrElse("message", "")
          )
        }
      }
      result.toOption
    }
  }
}
