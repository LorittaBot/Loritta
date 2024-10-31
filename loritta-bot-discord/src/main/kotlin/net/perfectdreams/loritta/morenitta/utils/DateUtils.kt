package net.perfectdreams.loritta.morenitta.utils

import net.dv8tion.jda.api.utils.TimeFormat
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.morenitta.utils.extensions.humanize
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.i18n.I18nKeysData
import java.time.*
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {
	/**
	 * Formats a [epochMilli] date into humanized string of "$absoluteTime ($relativeTime)"
	 *
	 * @param time    the offset date time
	 * @param locale  the locale that the data should be humanized in
	 * @return the humanized string
	 */
	fun formatDateWithRelativeFromNowAndAbsoluteDifference(locale: BaseLocale, i18nContext: I18nContext, time: OffsetDateTime) = formatDateWithRelativeFromNowAndAbsoluteDifference(locale, i18nContext, time.toInstant())

	/**
	 * Formats a [epochMilli] date into humanized string of "$absoluteTime ($relativeTime)"
	 *
	 * @param instant the current instant
	 * @param locale  the locale that the data should be humanized in
	 * @return the humanized string
	 */
	fun formatDateWithRelativeFromNowAndAbsoluteDifference(locale: BaseLocale, i18nContext: I18nContext, instant: Instant) = formatDateWithRelativeFromNowAndAbsoluteDifference(locale, i18nContext, instant.toEpochMilli())

	/**
	 * Formats a [epochMilli] date into humanized string of "$absoluteTime ($relativeTime)"
	 *
	 * @param epochMilli the time in milliseconds
	 * @param locale     the locale that the data should be humanized in
	 * @return the humanized string
	 */
	fun formatDateWithRelativeFromNowAndAbsoluteDifference(locale: BaseLocale, i18nContext: I18nContext, epochMilli: Long): String {
		val timeDifference = formatDateDiff(i18nContext, System.currentTimeMillis(), epochMilli)
		return "${epochMilli.humanize(locale)} ($timeDifference)"
	}

	/**
	 * Formats a [epochMilli] date into humanized string of "$absoluteTime ($relativeTime)" using Discord's markdown extensions
	 *
	 * @param epochMilli the time in milliseconds
	 * @return the humanized string
	 */
	fun formatDateWithRelativeFromNowAndAbsoluteDifference(epochMilli: Long): String {
		return "${TimeFormat.DATE_TIME_SHORT.format(epochMilli)} (${TimeFormat.RELATIVE.format(epochMilli)})"
	}

	/**
	 * Formats a [offsetDateTime] into humanized string of "$absoluteTime ($relativeTime)" using Discord's markdown extensions
	 *
	 * @param offsetDateTime the time
	 * @return the humanized string
	 */
	fun formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(offsetDateTime: OffsetDateTime) = formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(offsetDateTime.toInstant())


	/**
	 * Formats a [instant] into humanized string of "$absoluteTime ($relativeTime)" using Discord's markdown extensions
	 *
	 * @param instant the time
	 * @return the humanized string
	 */
	fun formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(instant: Instant) = formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(instant.toEpochMilli())

	/**
	 * Formats a [epochMilli] date into humanized string of "$absoluteTime ($relativeTime)" using Discord's markdown extensions
	 *
	 * @param epochMilli the time in milliseconds
	 * @return the humanized string
	 */
	fun formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(epochMilli: Long): String {
		return "${TimeFormat.DATE_TIME_SHORT.format(epochMilli)} (${TimeFormat.RELATIVE.format(epochMilli)})"
	}

	fun formatDiscordLikeRelativeDate(i18nContext: I18nContext, fromEpochMilli: Long, toEpochMilli: Long): String {
		val result = formatDateDiff(i18nContext, fromEpochMilli, toEpochMilli, 1)

		return if (fromEpochMilli > toEpochMilli) {
			i18nContext.get(I18nKeysData.Time.TimeSpanInTheFuture(result))
		} else {
			i18nContext.get(I18nKeysData.Time.TimeSpanInThePast(result))
		}
	}

	fun formatDateDiff(i18nContext: I18nContext, fromEpochMilli: Long, toEpochMilli: Long, maxParts: Int = Int.MAX_VALUE) = formatDateDiff(
		i18nContext,
		LocalDateTime.ofInstant(Instant.ofEpochMilli(fromEpochMilli), Constants.LORITTA_TIMEZONE),
		LocalDateTime.ofInstant(Instant.ofEpochMilli(toEpochMilli), Constants.LORITTA_TIMEZONE),
		maxParts
	)

	fun formatDateDiff(i18nContext: I18nContext, fromEpochMilli: Instant, toEpochMilli: Instant, maxParts: Int = Int.MAX_VALUE) = formatDateDiff(
		i18nContext,
		LocalDateTime.ofInstant(fromEpochMilli, Constants.LORITTA_TIMEZONE),
		LocalDateTime.ofInstant(toEpochMilli, Constants.LORITTA_TIMEZONE),
		maxParts
	)

	fun formatDateDiff(i18nContext: I18nContext, argumentFromDateTime: LocalDateTime, argumentToDateTime: LocalDateTime, maxParts: Int = Int.MAX_VALUE): String {
		// https://stackoverflow.com/a/59119149/7271796
		val fromDateTime: LocalDateTime
		val toDateTime: LocalDateTime

		// If the "from" time is in the future, we will invert the arguments to avoid the calculating failing with "a few milliseconds"
		if (argumentFromDateTime > argumentToDateTime) {
			toDateTime = argumentFromDateTime
			fromDateTime = argumentToDateTime
		} else {
			toDateTime = argumentToDateTime
			fromDateTime = argumentFromDateTime
		}

		// get the calendar period between the times (years, months & days)
		var period = Period.between(fromDateTime.toLocalDate(), toDateTime.toLocalDate())

		// make sure to get the floor of the number of days
		period = period.minusDays(if (toDateTime.toLocalTime() >= fromDateTime.toLocalTime()) 0 else 1)

		// get the remainder as a duration (hours, minutes, etc.)
		var duration: Duration = Duration.between(fromDateTime, toDateTime)

		// remove days, already counted in the period
		duration = duration.minusDays(duration.toDaysPart())

		val parts = mutableListOf<String>()

		fun createAndAdd(value: Int, key: (Int) -> (StringI18nData)) {
			if (value >= 1L)
				parts.add(i18nContext.get(key.invoke(value)))
		}

		createAndAdd(period.years) { I18nKeysData.Time.Years(it) }
		createAndAdd(period.months) { I18nKeysData.Time.Months(it) }
		createAndAdd(period.days) { I18nKeysData.Time.Days(it) }
		createAndAdd(duration.toHoursPart()) { I18nKeysData.Time.Hours(it) }
		createAndAdd(duration.toMinutesPart()) { I18nKeysData.Time.Minutes(it) }
		createAndAdd(duration.toSecondsPart()) { I18nKeysData.Time.Seconds(it) }

		return if (parts.isEmpty())
			i18nContext.get(I18nKeysData.Time.AFewMilliseconds)
		else
			parts.take(maxParts).joinToString(", ")
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