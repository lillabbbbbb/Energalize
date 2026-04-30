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

}