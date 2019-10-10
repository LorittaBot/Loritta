package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {
	private val maxYears = 100000

	fun dateDiff(type: Int, fromDate: Calendar, toDate: Calendar, future: Boolean): Int {
		val year = Calendar.YEAR

		val fromYear = fromDate.get(year)
		val toYear = toDate.get(year)
		if (Math.abs(fromYear - toYear) > maxYears) {
			toDate.set(year, fromYear + if (future) maxYears else -maxYears)
		}

		var diff = 0
		var savedDate = fromDate.timeInMillis
		while (future && !fromDate.after(toDate) || !future && !fromDate.before(toDate)) {
			savedDate = fromDate.timeInMillis
			fromDate.add(type, if (future) 1 else -1)
			diff++
		}
		diff--
		fromDate.timeInMillis = savedDate
		return diff
	}

	fun formatDateDiff(date: Long, locale: LegacyBaseLocale): String {
		val c = GregorianCalendar()
		c.timeInMillis = date
		val now = GregorianCalendar()
		return formatDateDiff(now, c, locale)
	}

	fun formatDateDiff(fromDate: Long, toDate: Long, locale: LegacyBaseLocale): String {
		val c = GregorianCalendar()
		c.timeInMillis = fromDate
		val now = GregorianCalendar()
		now.timeInMillis = toDate
		return formatDateDiff(now, c, locale)
	}

	fun formatDateDiff(fromDate: Calendar, toDate: Calendar, locale: LegacyBaseLocale): String {
		var future = false
		if (toDate == fromDate) {
			return locale["DATEUTILS_Now"]
		}
		if (toDate.after(fromDate)) {
			future = true
		}
		val sb = StringBuilder()
		val types = intArrayOf(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND)
		val names = arrayOf(locale["DATEUTILS_Year"], locale["DATEUTILS_Years"], locale["DATEUTILS_Month"], locale["DATEUTILS_Months"], locale["DATEUTILS_Day"], locale["DATEUTILS_Days"], locale["DATEUTILS_Hour"], locale["DATEUTILS_Hours"], locale["DATEUTILS_Minute"], locale["DATEUTILS_Minutes"], locale["DATEUTILS_Second"], locale["DATEUTILS_Seconds"])
		var accuracy = 0
		for (i in types.indices) {
			if (accuracy > 2) {
				break
			}
			val diff = dateDiff(types[i], fromDate, toDate, future)
			if (diff > 0) {
				accuracy++
				sb.append(" ").append(diff).append(" ").append(names[i * 2 + (if (diff > 1) 1 else 0)])
			}
		}
		return if (sb.isEmpty()) {
			locale["DATEUTILS_Now"]
		} else sb.toString().trim { it <= ' ' }
	}

	fun formatMillis(timeInMillis: Long, locale: LegacyBaseLocale): String {
		var jvmUpTime = timeInMillis
		val days = TimeUnit.MILLISECONDS.toDays(jvmUpTime)
		jvmUpTime -= TimeUnit.DAYS.toMillis(days)
		val hours = TimeUnit.MILLISECONDS.toHours(jvmUpTime)
		jvmUpTime -= TimeUnit.HOURS.toMillis(hours)
		val minutes = TimeUnit.MILLISECONDS.toMinutes(jvmUpTime)
		jvmUpTime -= TimeUnit.MINUTES.toMillis(minutes)
		val seconds = TimeUnit.MILLISECONDS.toSeconds(jvmUpTime)

		val sb = StringBuilder()
		if (days != 0L) {
			sb.append(days)
			val isPlural = days != 1L
			sb.append(" ")
			if (!isPlural) {
				sb.append(locale["DATEUTILS_Day"])
			} else {
				sb.append(locale["DATEUTILS_Days"])
			}
			sb.append(" ")
		}

		if (hours != 0L) {
			sb.append(hours)
			val isPlural = hours != 1L
			sb.append(" ")
			if (!isPlural) {
				sb.append(locale["DATEUTILS_Hour"])
			} else {
				sb.append(locale["DATEUTILS_Hours"])
			}
			sb.append(" ")
		}

		if (minutes != 0L) {
			sb.append(minutes)
			val isPlural = minutes != 1L
			sb.append(" ")
			if (!isPlural) {
				sb.append(locale["DATEUTILS_Minute"])
			} else {
				sb.append(locale["DATEUTILS_Minutes"])
			}
			sb.append(" ")
		}

		if (seconds != 0L) {
			sb.append(seconds)
			val isPlural = seconds != 1L
			sb.append(" ")
			if (!isPlural) {
				sb.append(locale["DATEUTILS_Second"])
			} else {
				sb.append(locale["DATEUTILS_Seconds"])
			}
			sb.append(" ")
		}
		return sb.toString().trim()
	}
}