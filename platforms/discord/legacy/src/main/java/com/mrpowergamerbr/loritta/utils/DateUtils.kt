package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.utils.extensions.humanize
import net.perfectdreams.loritta.common.locale.BaseLocale
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {
	private const val maxYears = 100000

	/**
	 * Formats a [epochMilli] date into humanized string of "$absoluteTime ($relativeTime)"
	 *
	 * @param time    the offset date time
	 * @param locale  the locale that the data should be humanized in
	 * @return the humanized string
	 */
	fun formatDateWithRelativeFromNowAndAbsoluteDifference(time: OffsetDateTime, locale: BaseLocale) = formatDateWithRelativeFromNowAndAbsoluteDifference(time.toInstant(), locale)

	/**
	 * Formats a [epochMilli] date into humanized string of "$absoluteTime ($relativeTime)"
	 *
	 * @param instant the current instant
	 * @param locale  the locale that the data should be humanized in
	 * @return the humanized string
	 */
	fun formatDateWithRelativeFromNowAndAbsoluteDifference(instant: Instant, locale: BaseLocale) = formatDateWithRelativeFromNowAndAbsoluteDifference(instant.toEpochMilli(), locale)

	/**
	 * Formats a [epochMilli] date into humanized string of "$absoluteTime ($relativeTime)"
	 *
	 * @param epochMilli the time in milliseconds
	 * @param locale     the locale that the data should be humanized in
	 * @return the humanized string
	 */
	fun formatDateWithRelativeFromNowAndAbsoluteDifference(epochMilli: Long, locale: BaseLocale): String {
		val timeDifference = formatDateDiff(epochMilli, locale)
		return "${epochMilli.humanize(locale)} ($timeDifference)"
	}

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
			return locale["loritta.date.aFewMilliseconds"]
		}
		if (toDate.after(fromDate)) {
			future = true
		}
		val sb = StringBuilder()
		val types = intArrayOf(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND)
		val names = arrayOf(locale["loritta.date.year"], locale["loritta.date.years"], locale["loritta.date.month"], locale["loritta.date.months"], locale["loritta.date.day"], locale["loritta.date.days"], locale["loritta.date.hour"], locale["loritta.date.hours"], locale["loritta.date.minute"], locale["loritta.date.minutes"], locale["loritta.date.second"], locale["loritta.date.seconds"])
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
			locale["loritta.date.aFewMilliseconds"]
		} else sb.toString().trim { it <= ' ' }
	}

	fun formatMillis(timeInMillis: Long, locale: BaseLocale): String {
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
				sb.append(locale["loritta.date.day"])
			} else {
				sb.append(locale["loritta.date.days"])
			}
			sb.append(" ")
		}

		if (hours != 0L) {
			sb.append(hours)
			val isPlural = hours != 1L
			sb.append(" ")
			if (!isPlural) {
				sb.append(locale["loritta.date.hour"])
			} else {
				sb.append(locale["loritta.date.hours"])
			}
			sb.append(" ")
		}

		if (minutes != 0L) {
			sb.append(minutes)
			val isPlural = minutes != 1L
			sb.append(" ")
			if (!isPlural) {
				sb.append(locale["loritta.date.minute"])
			} else {
				sb.append(locale["loritta.date.minutes"])
			}
			sb.append(" ")
		}

		if (seconds != 0L) {
			sb.append(seconds)
			val isPlural = seconds != 1L
			sb.append(" ")
			if (!isPlural) {
				sb.append(locale["loritta.date.second"])
			} else {
				sb.append(locale["loritta.date.seconds"])
			}
			sb.append(" ")
		}
		return sb.toString().trim()
	}
}