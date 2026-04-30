import java.time.format.DateTimeFormatter

import model.{EnergySource, PlantAsset}

object RepConstants {
  val DemandAssetId = "DEMAND"

  val DateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
  val TimestampFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

  val GeneratorsStatePath = "rep_generators.csv"
  val ReadingsStatePath = "rep_readings.csv"
  val AlertsStatePath = "rep_alerts.csv"

  val DefaultAssets = List(
    PlantAsset("SOLAR-1", "Solar Array A", EnergySource.Solar, "North Field", 2500.0, isEnabled = true, outputFactor = 1.0),
    PlantAsset("WIND-1", "Wind Turbine A", EnergySource.Wind, "Hill Ridge", 3200.0, isEnabled = true, outputFactor = 1.0),
    PlantAsset("HYDRO-1", "Hydro Turbine A", EnergySource.Hydro, "River Gate", 4100.0, isEnabled = true, outputFactor = 1.0)
  )
}
