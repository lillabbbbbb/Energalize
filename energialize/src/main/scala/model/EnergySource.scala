package model

sealed trait EnergySource
object EnergySource {
  case object Solar extends EnergySource
  case object Wind extends EnergySource
  case object Hydro extends EnergySource
}
