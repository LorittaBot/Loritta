package net.perfectdreams.loritta.utils

import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*



object CalendarUtils {
	/**
	 * Resets the current [calendar] to the beginning of the day (00:00)
	 *
	 * @param calendar the calendar that will be reset
	 *
	 * @return the calendar object
	 */
	fun resetToBeginningOfTheDay(calendar: Calendar): Calendar {
		calendar.set(Calendar.HOUR_OF_DAY, 0)
		calendar.set(Calendar.MINUTE, 0)
		calendar.set(Calendar.SECOND, 0)
		calendar.set(Calendar.MILLISECOND, 0)
		return calendar
	}

	/**
	 * Resets the current [calendar] to the end of the day (23:59)
	 *
	 * @param calendar the calendar that will be set
	 *
	 * @return the calendar object
	 */
	fun resetToEndOfTheDay(calendar: Calendar): Calendar {
		calendar.set(Calendar.HOUR_OF_DAY, 23)
		calendar.set(Calendar.MINUTE, 59)
		calendar.set(Calendar.SECOND, 59)
		calendar.set(Calendar.MILLISECOND, 999)
		return calendar
	}

	/**
	 * Gets the time in milliseconds until midnight
	 */
	fun getTimeUntilMidnight(): Long {
		val now = ZonedDateTime.now()

		val tomorrow = now.toLocalDate().plusDays(1)
		val tomorrowStart = tomorrow.atStartOfDay()

		val duration = Duration.between(now, tomorrowStart)
		return duration.toMillis()
	}

	/**
	 * Gets the time in milliseconds until midnight
	 */
	fun getTimeUntilMidnight(timeZone: ZoneId): Long {
		val now = ZonedDateTime.now(timeZone)

		val tomorrow = now.toLocalDate().plusDays(1)
		val tomorrowStart = tomorrow.atStartOfDay(timeZone)

		val duration = Duration.between(now, tomorrowStart)
		return duration.toMillis()
	}
}