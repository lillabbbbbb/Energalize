package model

final case class PlantAsset(
  id: String,
  name: String,
  source: EnergySource,
  location: String,
  ratedCapacityKw: Double
)
