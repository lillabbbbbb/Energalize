package reps.infrastructure.api

import reps.domain.model.{AppError, EnergyReading}

trait EnergyDataProvider {
  def fetchReadings(): Either[AppError, List[EnergyReading]]
}
