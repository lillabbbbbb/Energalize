package model

final case class AppState(
  assets: List[PlantAsset],
  readings: List[EnergyReading],
  alerts: List[Alert]
)
