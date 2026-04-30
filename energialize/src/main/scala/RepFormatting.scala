import model.{Alert, EnergyReading, EnergySource, HealthStatus, PlantAsset, StatsSummary}

object RepFormatting {
  def formatDouble(value: Double): String = f"$value%.2f"

  def formatSource(source: EnergySource): String = source match {
    case EnergySource.Solar => "Solar"
    case EnergySource.Wind => "Wind"
    case EnergySource.Hydro => "Hydro"
  }

  def formatHealthStatus(status: HealthStatus): String = status match {
    case HealthStatus.Healthy => "Healthy"
    case HealthStatus.Warning => "Warning"
    case HealthStatus.Malfunction => "Malfunction"
  }

  def formatAsset(asset: PlantAsset): String = {
    val status = if (asset.isEnabled) "ON" else "OFF"
    val output = formatDouble(asset.outputFactor * 100.0)
    s"${asset.id} | ${asset.name} | ${formatSource(asset.source)} | ${asset.location} | Rated ${formatDouble(asset.ratedCapacityKw)} kW | $status | Output ${output}%"
  }

  def formatReading(reading: EnergyReading): String = {
    val storage = reading.storageCapacityKwh.map(v => s"${formatDouble(v)} kWh").getOrElse("n/a")
    val notes = reading.notes.getOrElse("-")
    s"${reading.assetId} | ${reading.timestamp.format(RepConstants.TimestampFormatter)} | ${formatSource(reading.source)} | ${formatDouble(reading.energyKwh)} kWh | Storage: $storage | ${formatHealthStatus(reading.healthStatus)} | $notes"
  }

  def formatAlert(alert: Alert): String = {
    s"${alert.severity} | ${alert.assetId} | ${alert.timestamp.format(RepConstants.TimestampFormatter)} | ${alert.message}"
  }

  def formatStats(summary: StatsSummary): String = {
    val modeText = if (summary.mode.isEmpty) "n/a" else summary.mode.map(formatDouble).mkString(", ")
    s"""Count: ${summary.count}
       |Mean: ${formatDouble(summary.mean)}
       |Median: ${formatDouble(summary.median)}
       |Mode: $modeText
       |Range: ${formatDouble(summary.range)}
       |Midrange: ${formatDouble(summary.midrange)}
       |Min: ${formatDouble(summary.min)}
       |Max: ${formatDouble(summary.max)}""".stripMargin
  }
}
