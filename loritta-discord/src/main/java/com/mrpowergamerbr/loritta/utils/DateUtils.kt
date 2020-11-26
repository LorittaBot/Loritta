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
			return locale.toNewLocale()["loritta.date.aFewMilliseconds"]
		}
		if (toDate.after(fromDate)) {
			future = true
		}
		val sb = StringBuilder()
		val types = intArrayOf(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND)
		val names = arrayOf(locale.toNewLocale()["loritta.date.year"], locale.toNewLocale()["loritta.date.years"], locale.toNewLocale()["loritta.date.month"], locale.toNewLocale()["loritta.date.months"], locale.toNewLocale()["loritta.date.day"], locale.toNewLocale()["loritta.date.days"], locale.toNewLocale()["loritta.date.hour"], locale.toNewLocale()["loritta.date.hours"], locale.toNewLocale()["loritta.date.minute"], locale.toNewLocale()["loritta.date.minutes"], locale.toNewLocale()["loritta.date.second"], locale.toNewLocale()["loritta.date.seconds"])
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
			locale.toNewLocale()["loritta.date.aFewMilliseconds"]
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
				sb.append(locale.toNewLocale()["loritta.date.day"])
			} else {
				sb.append(locale.toNewLocale()["loritta.date.days"])
			}
			sb.append(" ")
		}

		if (hours != 0L) {
			sb.append(hours)
			val isPlural = hours != 1L
			sb.append(" ")
			if (!isPlural) {
				sb.append(locale.toNewLocale()["loritta.date.hour"])
			} else {
				sb.append(locale.toNewLocale()["loritta.date.hours"])
			}
			sb.append(" ")
		}

		if (minutes != 0L) {
			sb.append(minutes)
			val isPlural = minutes != 1L
			sb.append(" ")
			if (!isPlural) {
				sb.append(locale.toNewLocale()["loritta.date.minute"])
			} else {
				sb.append(locale.toNewLocale()["loritta.date.minutes"])
			}
			sb.append(" ")
		}

		if (seconds != 0L) {
			sb.append(seconds)
			val isPlural = seconds != 1L
			sb.append(" ")
			if (!isPlural) {
				sb.append(locale.toNewLocale()["loritta.date.second"])
			} else {
				sb.append(locale.toNewLocale()["loritta.date.seconds"])
			}
			sb.append(" ")
		}
		return sb.toString().trim()
	}
}