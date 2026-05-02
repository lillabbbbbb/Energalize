// PlantServices: CSV-backed implementations of PlantUseCases.
// Provides loading of assets/readings and basic analytics (overview, filtering, stats, alerts).

import java.io.File
import java.time.LocalDateTime
import java.time.temporal.WeekFields
import scala.util.Try

import com.github.tototoshi.csv._

import application.PlantUseCases
import model._

// CSV-backed service implementing PlantUseCases
object PlantServices extends PlantUseCases {
  // Load assets from CSV path and parse into PlantAsset
  override def loadAssets(path: String): Either[AppError, List[PlantAsset]] = {
    val result = Try {
      val reader = CSVReader.open(new File(path))
      val rows = reader.allWithHeaders()
      reader.close()
      rows.map { row =>
        val source = RepParsing.parseEnergySource(row.getOrElse("source", ""))
        val isEnabled = row.get("isEnabled").forall(_.toBooleanOption.getOrElse(true))
        val outputFactor = row.get("outputFactor").flatMap(v => Try(v.toDouble).toOption).getOrElse(1.0)
        PlantAsset(
          id = row.getOrElse("id", ""),
          name = row.getOrElse("name", ""),
          source = source,
          location = row.getOrElse("location", ""),
          ratedCapacityKw = row.getOrElse("ratedCapacityKw", "0").toDouble,
          isEnabled = isEnabled,
          outputFactor = outputFactor
        )
      }
    }

    result.toEither.left.map(_ => FileReadError(path))
  }

  // Load readings from CSV and parse into EnergyReading
  override def loadReadings(path: String): Either[AppError, List[EnergyReading]] = {
    val result = Try {
      val reader = CSVReader.open(new File(path))
      val rows = reader.allWithHeaders()
      reader.close()
      rows.map { row =>
        val timestamp = LocalDateTime.parse(row.getOrElse("timestamp", ""), RepConstants.TimestampFormatter)
        EnergyReading(
          assetId = row.getOrElse("assetId", ""),
          timestamp = timestamp,
          source = RepParsing.parseEnergySource(row.getOrElse("source", "")),
          energyKwh = row.getOrElse("energyKwh", "0").toDouble,
          storageCapacityKwh = row.get("storageCapacityKwh").filter(_.nonEmpty).map(_.toDouble),
          healthStatus = RepParsing.parseHealthStatus(row.getOrElse("healthStatus", "")),
          notes = row.get("notes").filter(_.nonEmpty)
        )
      }
    }

    result.toEither.left.map(_ => FileReadError(path))
  }

  // Simple one-line overview used by the CLI
  override def overview(assets: List[PlantAsset], readings: List[EnergyReading]): String = {
    val totalEnergy = readings.filterNot(RepLogic.isDemandReading).map(_.energyKwh).sum
    val totalStorage = readings.flatMap(_.storageCapacityKwh).sum
    s"Assets: ${assets.size} | Total Energy (kWh): ${RepFormatting.formatDouble(totalEnergy)} | Storage (kWh): ${RepFormatting.formatDouble(totalStorage)}"
  }

  // Filter readings by granularity (hour/day/week/month) using parsed date input
  override def filterByPeriod(
    readings: List[EnergyReading],
    granularity: TimeGranularity,
    dateInput: String
  ): Either[AppError, List[EnergyReading]] = {
    RepParsing.parseDate(dateInput).map { date =>
      granularity match {
        case TimeGranularity.Hourly =>
          readings.filter(_.timestamp.toLocalDate == date)
        case TimeGranularity.Daily =>
          readings.filter(_.timestamp.toLocalDate == date)
        case TimeGranularity.Weekly =>
          val weekFields = WeekFields.ISO
          val weekOfYear = date.get(weekFields.weekOfWeekBasedYear())
          val year = date.getYear
          readings.filter { reading =>
            val rDate = reading.timestamp.toLocalDate
            rDate.getYear == year && rDate.get(weekFields.weekOfWeekBasedYear()) == weekOfYear
          }
        case TimeGranularity.Monthly =>
          readings.filter { reading =>
            val rDate = reading.timestamp.toLocalDate
            rDate.getYear == date.getYear && rDate.getMonth == date.getMonth
          }
      }
    }
  }

  // Compute basic statistics (mean, median, mode, range, midrange, min, max, count)
  override def stats(readings: List[EnergyReading]): Either[AppError, StatsSummary] = {
    val values = readings.map(_.energyKwh)
    if (values.isEmpty) {
      Left(NoDataFound("selected readings"))
    } else {
      val sorted = values.sorted
      val count = sorted.size
      val mean = sorted.sum / count
      val median = if (count % 2 == 0) {
        val upper = sorted(count / 2)
        val lower = sorted((count / 2) - 1)
        (upper + lower) / 2
      } else {
        sorted(count / 2)
      }
      val grouped = values.groupBy(identity).view.mapValues(_.size).toMap
      val maxCount = grouped.values.max
      val mode = grouped.collect { case (value, freq) if freq == maxCount => value }.toList.sorted
      val min = sorted.head
      val max = sorted.last
      val range = max - min
      val midrange = (min + max) / 2
      Right(StatsSummary(mean, median, mode, range, midrange, min, max, count))
    }
  }

  // Generate alerts from readings and asset thresholds/health status
  override def alerts(
    assets: List[PlantAsset],
    readings: List[EnergyReading]
  ): List[Alert] = {
    val assetMap = assets.map(asset => asset.id -> asset).toMap
    readings.flatMap { reading =>
      val baseAlerts = reading.healthStatus match {
        case HealthStatus.Malfunction =>
          List(Alert(reading.assetId, reading.timestamp, "HIGH", "Equipment malfunction reported."))
        case HealthStatus.Warning =>
          List(Alert(reading.assetId, reading.timestamp, "MEDIUM", "Sensor warning detected."))
        case HealthStatus.Healthy =>
          List.empty
      }

      val outputAlerts = assetMap.get(reading.assetId).toList.flatMap { asset =>
        val threshold = asset.ratedCapacityKw * 0.2
        if (reading.energyKwh < threshold) {
          List(
            Alert(
              reading.assetId,
              reading.timestamp,
              "MEDIUM",
              s"Low output (${RepFormatting.formatDouble(reading.energyKwh)} kWh) vs rated ${RepFormatting.formatDouble(asset.ratedCapacityKw)} kW."
            )
          )
        } else {
          List.empty
        }
      }

      baseAlerts ++ outputAlerts
    }
  }
}
