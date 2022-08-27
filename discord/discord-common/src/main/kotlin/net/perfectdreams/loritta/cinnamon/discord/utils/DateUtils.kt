package net.perfectdreams.loritta.cinnamon.discord.utils

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import kotlin.time.Duration.Companion.milliseconds

object DateUtils {
    fun formatDateDiff(i18nContext: I18nContext, fromDate: Long, toDate: Long): String {
        val diff = toDate - fromDate
        val duration = diff.milliseconds

        val parts = mutableListOf<String>()

        fun createAndAdd(value: Long, key: (Long) -> (StringI18nData)) {
            if (value >= 1L)
                parts.add(i18nContext.get(key.invoke(value)))
        }

        duration.toComponents { wholeDays, hours, minutes, seconds, _ ->
            val years = wholeDays / 365
            val days = wholeDays % 365

            createAndAdd(years) { I18nKeysData.Time.Years(it) }
            createAndAdd(days) { I18nKeysData.Time.Days(it) }
            createAndAdd(hours.toLong()) { I18nKeysData.Time.Hours(it) }
            createAndAdd(minutes.toLong()) { I18nKeysData.Time.Minutes(it) }
            createAndAdd(seconds.toLong()) { I18nKeysData.Time.Seconds(it) }
        }

        return if (parts.isEmpty())
            i18nContext.get(I18nKeysData.Time.AFewMilliseconds)
        else
            parts.joinToString(", ")
    }
}