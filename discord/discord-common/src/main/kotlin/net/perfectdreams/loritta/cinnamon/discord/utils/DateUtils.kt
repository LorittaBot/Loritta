package net.perfectdreams.loritta.cinnamon.discord.utils

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import java.time.*

object DateUtils {
    fun formatDateDiff(i18nContext: I18nContext, fromEpochMilli: Long, toEpochMilli: Long): String {
        // https://stackoverflow.com/a/59119149/7271796
        val fromDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fromEpochMilli), ZoneId.of("America/Sao_Paulo"))
        val toDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(toEpochMilli), ZoneId.of("America/Sao_Paulo"))

        // get the calendar period between the times (years, months & days)
        var period: Period = Period.between(fromDateTime.toLocalDate(), toDateTime.toLocalDate())

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
            parts.joinToString(", ")
    }
}