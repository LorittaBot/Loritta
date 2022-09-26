package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.perfectdreams.i18nhelper.core.I18nContext
import kotlin.js.Date
import kotlin.math.absoluteValue
import kotlin.math.floor

fun timeDifference(i18nContext: I18nContext, instant: Instant): String {
    val formatId = i18nContext.language.info.formattingLanguageId

    // From https://stackoverflow.com/a/62029040/7271796
    val msPerMinute = 60 * 1000L
    val msPerHour = msPerMinute * 60
    val msPerDay = msPerHour * 24
    val msPerMonth = msPerDay * 30
    val msPerYear = msPerDay * 365

    val current = Clock.System.now().toEpochMilliseconds()
    val timestamp = instant.toEpochMilliseconds()
    val elapsed = current - timestamp
    val absoluteElapsed = elapsed.absoluteValue

    val dyn = {}.asDynamic()
    dyn.numeric = "auto"

    val rtf = Intl.RelativeTimeFormat(formatId, dyn)

    return when {
        msPerMinute > absoluteElapsed -> {
            rtf.format(-floor((elapsed/1000).toDouble()), "seconds")
        }
        msPerHour > absoluteElapsed -> {
            rtf.format(-floor((elapsed/msPerMinute).toDouble()), "minutes")
        }
        msPerDay > absoluteElapsed -> {
            rtf.format(-floor((elapsed/msPerHour).toDouble()), "hours")
        }
        msPerMonth > absoluteElapsed -> {
            rtf.format(-floor((elapsed/msPerDay).toDouble()), "days")
        }
        msPerYear > absoluteElapsed -> {
            rtf.format(-floor((elapsed/msPerMonth).toDouble()), "years")
        }
        else -> {
            Date(timestamp).toLocaleDateString(formatId)
        }
    }
}