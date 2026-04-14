package model

sealed trait HealthStatus
object HealthStatus {
  case object Healthy extends HealthStatus
  case object Warning extends HealthStatus
  case object Malfunction extends HealthStatus
}
