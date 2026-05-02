import java.time.{Duration, LocalDateTime}
import scala.util.Random

import model.{Alert, EnergyReading, HealthStatus, PlantAsset}

object RepLogic {
  def isDemandReading(reading: EnergyReading): Boolean = reading.assetId == RepConstants.DemandAssetId

  // Helper to convert the time data and capacity to a kwh reading
  def consumptionToKwh(valueMw: Double, start: LocalDateTime, end: LocalDateTime): Double = {
    val minutes = Duration.between(start, end).toMinutes
    val hours = math.max(0.0, minutes.toDouble / 60.0)
    valueMw * 1000.0 * hours
  }

  // Get the generated amount of energy
  def generateForDemand(assets: List[PlantAsset], demand: EnergyReading): List[EnergyReading] = {
    val availableAssets = assets.filter(_.isEnabled)
    val totalCapacity = availableAssets.map(asset => asset.ratedCapacityKw * asset.outputFactor).sum
    if (availableAssets.isEmpty || totalCapacity <= 0.0) {
      List.empty
    } else {
      availableAssets.map { asset =>
        val effectiveCapacity = asset.ratedCapacityKw * asset.outputFactor
        val share = effectiveCapacity / totalCapacity
        demand.copy(
          assetId = asset.id,
          source = asset.source,
          energyKwh = demand.energyKwh * share,
          storageCapacityKwh = None,
          healthStatus = HealthStatus.Healthy,
          notes = Some("Generated to meet demand")
        )
      }
    }
  }

  // Function to create random errors with 25% chance
  def generateRandomAlerts(assets: List[PlantAsset]): List[Alert] = {
    if (assets.isEmpty) {
      List.empty
    } else {
      val rng = new Random()
      val count = assets.count(_ => rng.nextDouble() < 0.25)
      val severities = List("LOW", "MEDIUM", "HIGH")
      val messages = List(
        "Random sensor anomaly detected.",
        "Intermittent communication timeout.",
        "Output fluctuation outside expected range."
      )
      (0 until count).toList.map { _ =>
        val asset = assets(rng.nextInt(assets.size))
        val severity = severities(rng.nextInt(severities.size))
        val message = messages(rng.nextInt(messages.size))
        Alert(asset.id, LocalDateTime.now(), severity, message)
      }
    }
  }
}
