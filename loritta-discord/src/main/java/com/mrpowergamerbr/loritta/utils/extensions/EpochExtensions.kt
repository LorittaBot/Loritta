package com.mrpowergamerbr.loritta.utils.extensions

import net.perfectdreams.loritta.common.locale.BaseLocale
import java.text.DateFormatSymbols
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*

/**
 * "Humanizes" the date
 *
 * @param locale the language that should be used to humanize the date
 * @return       the humanized date
 */
fun OffsetDateTime.humanize(locale: BaseLocale): String {
	val localeId = locale.id
	val fixedOffset = this.atZoneSameInstant(ZoneId.systemDefault()).toOffsetDateTime()
	val javaLocale = when (localeId) {
		"default" -> Locale("pt", "br")
		else -> Locale(localeId.split("-")[0], localeId.split("-").getOrNull(1) ?: "")
	}
	val months = DateFormatSymbols(javaLocale).months


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