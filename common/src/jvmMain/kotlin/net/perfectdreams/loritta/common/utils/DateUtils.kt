package net.perfectdreams.loritta.common.utils

import net.perfectdreams.loritta.common.locale.BaseLocale
import java.text.DateFormatSymbols
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

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
        if (abs(fromYear - toYear) > maxYears) {
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
        when {
            days != 0L -> sb.append(formatTimeUnit("day", days, locale))
            hours != 0L -> sb.append(formatTimeUnit("hour", hours, locale))
            minutes != 0L -> sb.append(formatTimeUnit("minute", minutes, locale))
            seconds != 0L -> sb.append(formatTimeUnit("second", seconds, locale))
        }
        return sb.toString().trim()
    }

    private fun formatTimeUnit(name: String, time: Long, locale: BaseLocale) = buildString {
        append(time)
        val isPlural = time != 1L
        append(" ")
        if (!isPlural) {
            append(locale["loritta.date.$name"])
        } else {
            append(locale["loritta.date.${name}s"])
        }
        append(" ")
    }
}

/**
 * "Humanizes" the date
 *
 * @param locale the language that should be used to humanize the date
 * @return       the humanized date
 */
fun OffsetDateTime.humanize(locale: BaseLocale): String {
    val localeId = locale.id
    val fixedOffset = this.atZoneSameInstant(ZoneId.systemDefault()).toOffsetDateTime()
    val months = DateFormatSymbols(Locale(localeId)).months

    return if (localeId == "en-us") {
        val fancy = when (this.dayOfMonth) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
        "${this.dayOfMonth}$fancy of ${months[this.month.value - 1]}, ${fixedOffset.year} at ${fixedOffset.hour.toString().padStart(2, '0')}:${fixedOffset.minute.toString().padStart(2, '0')}"
    } else {
        "${this.dayOfMonth} de ${months[this.month.value - 1]}, ${fixedOffset.year} às ${fixedOffset.hour.toString().padStart(2, '0')}:${fixedOffset.minute.toString().padStart(2, '0')}"
    }
}

/**
 * "Humanizes" the date
 *
 * @param locale the language that should be used to humanize the date
 * @return       the humanized date
 */
fun Long.humanize(locale: BaseLocale): String {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toOffsetDateTime().humanize(locale)
}