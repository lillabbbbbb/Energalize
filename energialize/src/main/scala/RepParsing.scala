import java.time.LocalDate
import java.util.Locale
import scala.util.Try

import model.{AppError, EnergySource, HealthStatus, InvalidDateFormat}

// Helpers for parsing data in from the CSV files

object RepParsing {
  def parseDate(input: String): Either[AppError, LocalDate] = {
    Try(LocalDate.parse(input, RepConstants.DateFormatter)).toEither.left.map(_ => InvalidDateFormat(input))
  }

  def parseEnergySource(input: String): EnergySource = {
    input.trim.toLowerCase(Locale.ROOT) match {
      case "solar" => EnergySource.Solar
      case "wind" => EnergySource.Wind
      case "hydro" => EnergySource.Hydro
      case _ => EnergySource.Solar
    }
  }

  def parseHealthStatus(input: String): HealthStatus = {
    input.trim.toLowerCase(Locale.ROOT) match {
      case "healthy" => HealthStatus.Healthy
      case "warning" => HealthStatus.Warning
      case "malfunction" => HealthStatus.Malfunction
      case _ => HealthStatus.Healthy
    }
  }
}
