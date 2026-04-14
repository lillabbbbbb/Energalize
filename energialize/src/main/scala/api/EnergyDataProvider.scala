package api

import model.{AppError, EnergyReading}

trait EnergyDataProvider {
  def fetchReadings(): Either[AppError, List[EnergyReading]]
}
