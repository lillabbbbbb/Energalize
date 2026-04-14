package reps.domain.model

import java.time.LocalDateTime

final case class EnergyReading(
  assetId: String,
  timestamp: LocalDateTime,
  source: EnergySource,
  energyKwh: Double,
  storageCapacityKwh: Option[Double],
  healthStatus: HealthStatus,
  notes: Option[String]
)
