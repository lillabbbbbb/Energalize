package reps.domain.model

import java.time.LocalDateTime

final case class Alert(
  assetId: String,
  timestamp: LocalDateTime,
  severity: String,
  message: String
)
