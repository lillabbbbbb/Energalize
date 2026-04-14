package model

sealed trait AppError {
  def message: String
}

final case class InvalidDateFormat(input: String) extends AppError {
  val message: String = s"Invalid date format: '$input'. Please use DD/MM/YYYY, for example 12/04/2024."
}

final case class NoDataFound(criteria: String) extends AppError {
  val message: String = s"No available data for: $criteria. Please choose another input."
}

final case class CsvParseError(line: String) extends AppError {
  val message: String = s"Failed to parse CSV line: $line"
}

final case class FileReadError(path: String) extends AppError {
  val message: String = s"Could not read file: $path"
}
