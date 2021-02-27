package net.perfectdreams.loritta.utils

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
}