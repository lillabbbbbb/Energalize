package reps.domain.model

sealed trait TimeGranularity
object TimeGranularity {
  case object Hourly extends TimeGranularity
  case object Daily extends TimeGranularity
  case object Weekly extends TimeGranularity
  case object Monthly extends TimeGranularity
}
