package functions

import java.time.{LocalDate, LocalDateTime, YearMonth}
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale
import scala.util.Try

import model.{AppError, EnergyReading, InvalidDateFormat, NoDataFound, TimeGranularity}

object DataAnalysis {

    // Statistical functions
    def mean(list: List[Double]): Double = if list.isEmpty 0.0 else list.sum/list.size // Scala Metals might throw a fit and ask for "then" after list.isEmpty, but IntelliJ does not have this problem and runs fine.

    def median(list: List[Double]): Double = {
        val listSize = list.size
        if (listSize == 0) 0.0 else {
            val sortedList = list.sorted
            if (listSize % 2 == 0) ((sortedList(listSize/2))+(sortedList(listSize/2-1)))/2 else sortedList(listSize/2)
        }
    }

	def mode(list: List[Double]): Double = { // source used: https://stackoverflow.com/questions/21998521/how-to-find-the-mode-value-of-a-list
			if (list.isEmpty) 0.0 else {
			// count occurrences
			val counts = list.groupBy(identity).map { case (k, v) => k -> v.size } // count
			val maxCount = counts.values.max // highest frequency from the counted occurrences
			// collect all values with highest frequency and return the smallest (deterministic)
			counts.collect { case (value, count) if count == maxCount => value }.min
		}
	}

    def range(list: List[Double]): Double = if list.isEmpty 0.0 else list.max-list.min

    def midrange(list: List[Double]): Double = if list.isEmpty 0.0 else (list.max+list.min)/2.0

    
    // Data filters that cover the hourly, daily, weekly, and monthly times.
def sortHourly(readings: List[EnergyReading], dateInput: String, ascending: Boolean = true): Either[AppError, List[EnergyReading]] =
    parseHourly(dateInput).flatMap { target =>
        val filtered = readings.filter { r =>
            r.timestamp.getYear         == target.getYear         &&
            r.timestamp.getMonthValue   == target.getMonthValue   &&
            r.timestamp.getDayOfMonth   == target.getDayOfMonth   &&
            r.timestamp.getHour         == target.getHour
        }
        if (filtered.isEmpty) Left(NoDataFound(s"hourly data for $dateInput"))
        else Right(sortReadings(filtered, ascending))
    }

def sortDaily(readings: List[EnergyReading], dateInput: String, ascending: Boolean = true): Either[AppError, List[EnergyReading]] =
    parseDay(dateInput).flatMap { target =>
        val filtered = readings.filter(_.timestamp.toLocalDate == target)
        if (filtered.isEmpty) Left(NoDataFound(s"daily data for $dateInput"))
        else Right(sortReadings(filtered, ascending))
    }

def sortWeekly(readings: List[EnergyReading], dateInput: String, ascending: Boolean = true): Either[AppError, List[EnergyReading]] =
    parseDay(dateInput).flatMap { target =>
        val weekFields = WeekFields.ISO
        val filtered = readings.filter { r =>
            r.timestamp.get(weekFields.weekBasedYear())      == target.get(weekFields.weekBasedYear()) &&
            r.timestamp.get(weekFields.weekOfWeekBasedYear()) == target.get(weekFields.weekOfWeekBasedYear())
        }
        if (filtered.isEmpty) Left(NoDataFound(s"weekly data for $dateInput"))
        else Right(sortReadings(filtered, ascending))
    }

def sortMonthly(readings: List[EnergyReading], dateInput: String, ascending: Boolean = true): Either[AppError, List[EnergyReading]] =
    parseMonth(dateInput).flatMap { target =>
        val filtered = readings.filter(r => YearMonth.from(r.timestamp) == target)
        if (filtered.isEmpty) Left(NoDataFound(s"monthly data for $dateInput"))
        else Right(sortReadings(filtered, ascending))
    }


// Privated helper functions for specifically parsing and to fulfill the sorting function condition laid out in Moodle.
private def sortReadings(readings: List[EnergyReading], ascending: Boolean): List[EnergyReading] =
    if (ascending) readings.sortBy(_.timestamp)
    else readings.sortBy(_.timestamp)(Ordering[LocalDateTime].reverse)

private def parseHourly(input: String): Either[AppError, LocalDateTime] = {
    val formatters = List(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm"),
        DateTimeFormatter.ofPattern("dd/MM/uuuu HH")
    )
    firstSuccessfulParse(input, formatters)(LocalDateTime.parse).toRight(InvalidDateFormat(input))
}

private def parseDay(input: String): Either[AppError, LocalDate] = {
    val formatters = List(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("dd/MM/uuuu")
    )
    firstSuccessfulParse(input, formatters)(LocalDate.parse).toRight(InvalidDateFormat(input))
}

private def parseMonth(input: String): Either[AppError, YearMonth] = {
    val formatters = List(
        DateTimeFormatter.ofPattern("uuuu-MM"),
        DateTimeFormatter.ofPattern("MM/uuuu")
    )
    firstSuccessfulParse(input, formatters)(YearMonth.parse).toRight(InvalidDateFormat(input))
}

private def firstSuccessfulParse[A](input: String, formatters: List[DateTimeFormatter])
    (parse: (String, DateTimeFormatter) => A): Option[A] =
    formatters.view.flatMap(f => Try(parse(input, f)).toOption).headOption


}