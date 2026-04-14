package reps.domain.model

final case class StatsSummary(
  mean: Double,
  median: Double,
  mode: List[Double],
  range: Double,
  midrange: Double,
  min: Double,
  max: Double,
  count: Int
)
