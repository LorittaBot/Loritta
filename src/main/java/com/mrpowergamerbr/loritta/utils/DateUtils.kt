package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.util.*

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

	fun formatDateDiff(date: Long, locale: BaseLocale): String {
		val c = GregorianCalendar()
		c.timeInMillis = date
		val now = GregorianCalendar()
		return formatDateDiff(now, c, locale)
	}

	fun formatDateDiff(fromDate: Long, toDate: Long, locale: BaseLocale): String {
		val c = GregorianCalendar()
		c.timeInMillis = fromDate
		val now = GregorianCalendar()
		now.timeInMillis = toDate
		return formatDateDiff(now, c, locale)
	}

	fun formatDateDiff(fromDate: Calendar, toDate: Calendar, locale: BaseLocale): String {
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
		return if (sb.length == 0) {
			locale["DATEUTILS_Now"]
		} else sb.toString().trim { it <= ' ' }
	}
}