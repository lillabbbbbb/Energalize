import java.time.{Instant, LocalDateTime, ZoneId}
import scala.util.Try

import sttp.client3._

import api.EnergyDataProvider
import model.{AppError, CsvParseError, EnergyReading, HealthStatus}

final case class FingridEnergyDataProvider(apiKey: String) extends EnergyDataProvider {
  private val backend = HttpURLConnectionBackend()
  private val consumptionPattern =
    """\{\s*"datasetId"\s*:\s*\d+\s*,\s*"startTime"\s*:\s*"([^"]+)"\s*,\s*"endTime"\s*:\s*"([^"]+)"\s*,\s*"value"\s*:\s*([0-9.]+)\s*\}""".r

  // Fetch raw readings from the Fingrid API and parse into demand EnergyReading entries -
  // actually uses electricity production from small-scale power plants data
  override def fetchReadings(): Either[AppError, List[EnergyReading]] = {
    val request = basicRequest
      .get(uri"https://data.fingrid.fi/api/datasets/205/data")
      .header("x-api-key", apiKey)

    val response = request.send(backend)
    response.body match {
      case Left(errorBody) =>
        Left(CsvParseError(s"Fingrid API error: $errorBody"))
      case Right(rawBody) =>
        parseConsumptionPayload(rawBody).map(_.map(toDemandReading))
    }
  }

  // Parse the API payload into ConsumptionSample records
  private def parseConsumptionPayload(rawBody: String): Either[AppError, List[ConsumptionSample]] = {
    val matches = consumptionPattern.findAllMatchIn(rawBody).toList
    if (matches.isEmpty) {
      Left(CsvParseError("Failed to parse Fingrid consumption payload."))
    } else {
      val parsed = matches.flatMap { m =>
        val startRaw = m.group(1)
        val endRaw = m.group(2)
        val valueMw = m.group(3).toDouble
        val start = parseUtcTimestamp(startRaw)
        val end = parseUtcTimestamp(endRaw)
        (start, end) match {
          case (Right(startTime), Right(endTime)) =>
            Some(ConsumptionSample(startTime, endTime, valueMw))
          case _ => None
        }
      }
      if (parsed.isEmpty) {
        Left(CsvParseError("Failed to parse Fingrid consumption timestamps."))
      } else {
        Right(parsed)
      }
    }
  }

  // Parse timestamp into LocalDateTime using system default zone
  private def parseUtcTimestamp(raw: String): Either[AppError, LocalDateTime] = {
    Try(Instant.parse(raw)).toEither.left.map(_ => CsvParseError(s"Invalid timestamp: $raw")).map { instant =>
      LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    }
  }

  // Convert a consumption sample into an internal EnergyReading
  private def toDemandReading(sample: ConsumptionSample): EnergyReading = {
    val demandKwh = RepLogic.consumptionToKwh(sample.valueMw, sample.startTime, sample.endTime)
    EnergyReading(
      assetId = RepConstants.DemandAssetId,
      timestamp = sample.startTime,
      source = model.EnergySource.Solar,
      energyKwh = demandKwh,
      storageCapacityKwh = None,
      healthStatus = HealthStatus.Healthy,
      notes = Some(s"Demand MW: ${RepFormatting.formatDouble(sample.valueMw)}")
    )
  }
}

final case class ConsumptionSample(
  startTime: LocalDateTime,
  endTime: LocalDateTime,
  valueMw: Double
)
