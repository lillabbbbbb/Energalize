package application

import model._

trait PlantUseCases {
  def loadAssets(path: String): Either[AppError, List[PlantAsset]]
  def loadReadings(path: String): Either[AppError, List[EnergyReading]]
  def overview(
    assets: List[PlantAsset],
    readings: List[EnergyReading]
  ): String
  def filterByPeriod(
    readings: List[EnergyReading],
    granularity: TimeGranularity,
    dateInput: String
  ): Either[AppError, List[EnergyReading]]
  def stats(readings: List[EnergyReading]): Either[AppError, StatsSummary]
  def alerts(
    assets: List[PlantAsset],
    readings: List[EnergyReading]
  ): List[Alert]
}
