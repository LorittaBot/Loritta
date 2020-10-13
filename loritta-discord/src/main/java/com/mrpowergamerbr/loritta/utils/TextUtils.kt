package com.mrpowergamerbr.loritta.utils

import java.net.URLEncoder
import java.text.MessageFormat
import java.util.*

val morseValues = mapOf(
		// ALFABETO
		'A' to ".-",
		'B' to "-...",
		'C' to "-.-.",
		'D' to "-..",
		'E' to ".",
		'F' to "..-.",
		'G' to "--.",
		'H' to "....",
		'I' to "..",
		'J' to ".---",
		'K' to "-.-",
		'L' to ".-..",
		'M' to "--",
		'N' to "-.",
		'O' to "---",
		'P' to ".--.",
		'Q' to "--.-",
		'R' to ".-.",
		'S' to "...",
		'T' to "-",
		'U' to "..-",
		'V' to "...-",
		'W' to ".--",
		'X' to "-..-",
		'Y' to "-.--",
		'Z' to "--..",

		// NÚMEROS
		'1' to "·----",
		'2' to "··---",
		'3' to "···--",
		'4' to "····-",
		'5' to "·····",
		'6' to "-····",
		'7' to "--···",
		'8' to "---··",
		'9' to "----·",
		'0' to "-----",

		// PONTUAÇÕES COMUNS
		'.' to "·-·-·-",
		',' to "--··--",
		'?' to "··--··",
		'\'' to "·----·",
		'!' to "-·-·--",
		'/' to "-··-·",
		'(' to "-·--·",
		')' to "-·--·-",
		'&' to "·-···",
		':' to "---···",
		';' to "-·-·-·",
		'=' to "-···-",
		'-' to "-····-",
		'_' to "··--·-",
		'"' to "·-··-·",
		'$' to "···-··-",
		'@' to "·--·-·",
		' ' to "/",

		// OUTROS CARACTERES
		'ä' to "·-·-",
		'à' to "·--·-",
		'ç' to "-·-··",
		'ð' to "··--·",
		'è' to "·-··-",
		'é' to "··-··",
		'ĝ' to "--·-·",
		'ĥ' to "-·--·",
		'ĵ' to "·---·",
		'ñ' to "--·--",
		'ö' to "---·",
		'ŝ' to "···-·",
		'þ' to "·--··",
		'ü' to "··--"
)

fun String.stripNewLines(): String {
	return this.replace(Regex("[\\r\\n]"), "")
}

fun String.encodeToUrl(enc: String = "UTF-8"): String {
	return URLEncoder.encode(this, enc)
}

fun String.stripCodeMarks(): String {
	return this.replace("`", "")
}

fun String.stripZeroWidthSpace(): String {
	return this.replace("\u200B", "")
}

fun String.msgFormat(vararg arguments: Any?): String {
	return MessageFormat.format(this, *arguments)
}

fun String.f(vararg arguments: Any?): String {
	return msgFormat(*arguments)
}

fun String.substringIfNeeded(range: IntRange = 0 until 2000, suffix: String = "..."): String {
	if (this.isEmpty()) {
		return this
	}

	if (this.length - 1 in range)
		return this

	// We have a Math.max to avoid issues when the string is waaaay too small, causing the range.last - suffix.length be negative
	return this.substring(range.start .. Math.max(0, range.last - suffix.length)) + suffix
}

val TIME_PATTERN = "(([01]\\d|2[0-3]):([0-5]\\d)(:([0-5]\\d))?) ?(am|pm)?".toPattern()
val DATE_PATTERN = "(0[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012])[- /.]([0-9]+)".toPattern()
val YEAR_PATTERN = "([0-9]+) ?(y|a)".toPattern()
val MONTH_PATTERN = "([0-9]+) ?(month(s)?|m(e|ê)s(es)?)".toPattern()
val WEEK_PATTERN = "([0-9]+) ?(w)".toPattern()
val DAY_PATTERN = "([0-9]+) ?(d)".toPattern()
val HOUR_PATTERN = "([0-9]+) ?(h)".toPattern()
val SHORT_MINUTE_PATTERN = "([0-9]+) ?(m)".toPattern()
val MINUTE_PATTERN = "([0-9]+) ?(min)".toPattern()
val SECONDS_PATTERN = "([0-9]+) ?(s)".toPattern()

fun String.convertToEpochMillisRelativeToNow(): Long {
	val content = this.toLowerCase()
	val calendar = Calendar.getInstance()
	var foundViaTime = false

	if (content.contains(":")) { // horário
		val matcher = TIME_PATTERN.matcher(content)

		if (matcher.find()) { // Se encontrar...
			val hour = matcher.group(2).toIntOrNull() ?: 0
			val minute = matcher.group(3).toIntOrNull() ?: 0
			val seconds = try {
				matcher.group(5)?.toIntOrNull() ?: 0
			} catch (e: IllegalStateException) {
				0
			}

			var meridiem = try {
				matcher.group(6)
			} catch (e: IllegalStateException) {
				null
			}

			// Horários que usam o meridiem
			if (meridiem != null) {
				meridiem = meridiem.replace(".", "").replace(" ", "")
				if (meridiem.equals("pm", true)) { // Se for PM, aumente +12
					calendar[Calendar.HOUR_OF_DAY] = (hour % 12) + 12
				} else { // Se for AM, mantenha do jeito atual
					calendar[Calendar.HOUR_OF_DAY] = (hour % 12)
				}
			} else {
				calendar[Calendar.HOUR_OF_DAY] = hour
			}
			calendar[Calendar.MINUTE] = minute
			calendar[Calendar.SECOND] = seconds
			foundViaTime = true
		}
	}

	if (content.contains("/")) { // data
		val matcher = DATE_PATTERN.matcher(content)

		if (matcher.find()) { // Se encontrar...
			val day = matcher.group(1).toIntOrNull() ?: 1
			val month = matcher.group(2).toIntOrNull() ?: 1
			val year = matcher.group(3).toIntOrNull() ?: 1999

			calendar[Calendar.DAY_OF_MONTH] = day
			calendar[Calendar.MONTH] = month - 1
			calendar[Calendar.YEAR] = year
		}
	} else if (foundViaTime && Calendar.getInstance().timeInMillis >= calendar.timeInMillis) {
		// If it was found via time but there isn't any day set, we are going to check if it is in the past and, if true, we are going to add one day
		calendar.add(Calendar.DAY_OF_MONTH, 1)
	}

	val yearsMatcher = YEAR_PATTERN.matcher(content)
	if (yearsMatcher.find()) {
		val addYears = yearsMatcher.group(1).toIntOrNull() ?: 0
		calendar[Calendar.YEAR] += addYears
	}
	val monthMatcher = MONTH_PATTERN.matcher(content)
	var foundMonths = false
	if (monthMatcher.find()) {
		foundMonths = true
		val addMonths = monthMatcher.group(1).toIntOrNull() ?: 0
		calendar[Calendar.MONTH] += addMonths
	}
	val weekMatcher = WEEK_PATTERN.matcher(content)
	if (weekMatcher.find()) {
		val addWeeks = weekMatcher.group(1).toIntOrNull() ?: 0
		calendar[Calendar.WEEK_OF_YEAR] += addWeeks
	}
	val dayMatcher = DAY_PATTERN.matcher(content)
	if (dayMatcher.find()) {
		val addDays = dayMatcher.group(1).toIntOrNull() ?: 0
		calendar[Calendar.DAY_OF_YEAR] += addDays
	}
	val hourMatcher = HOUR_PATTERN.matcher(content)
	if (hourMatcher.find()) {
		val addHours = hourMatcher.group(1).toIntOrNull() ?: 0
		calendar[Calendar.HOUR_OF_DAY] += addHours
	}

	// This check is needed due to the month pattern also checking for "m"
	// So, if the month was not found, we will check with the short minute pattern
	// If it was found, we will ignore the short minute pattern and only use the long pattern
	var foundMinutes = false
	if (!foundMonths) {
		val minuteMatcher = SHORT_MINUTE_PATTERN.matcher(content)
		if (minuteMatcher.find()) {
			foundMinutes = true
			val addMinutes = minuteMatcher.group(1).toIntOrNull() ?: 0
			calendar[Calendar.MINUTE] += addMinutes
		}
	}

	if (!foundMinutes) {
		val minuteMatcher = MINUTE_PATTERN.matcher(content)
		if (minuteMatcher.find()) {
			val addMinutes = minuteMatcher.group(1).toIntOrNull() ?: 0
			calendar[Calendar.MINUTE] += addMinutes
		}
	}

	val secondsMatcher = SECONDS_PATTERN.matcher(content)
	if (secondsMatcher.find()) {
		val addSeconds = secondsMatcher.group(1).toIntOrNull() ?: 0
		calendar[Calendar.SECOND] += addSeconds
	}

	return calendar.timeInMillis
}

fun String.escapeMentions(): String {
	return this.replace(Regex("\\\\+@"), "@").replace("@", "@\u200B")
}

fun String.fromMorse(): String {
	// Criar uma string vazia para guardar a nossa mensagem em texto comum
	var text = ""

	// Separar nossa string em morse em espaços para fazer um for nela
	this.split(" ").forEach { inMorse ->
		// Pegar o valor do char em morse
		val inTextEntry = morseValues.entries.firstOrNull { it.value.equals(inMorse) }

		if (inTextEntry != null) { // E, caso seja diferente de null...
			text += inTextEntry.key // Pegar o nosso valor e colocar na nossa string!
		}
	}
	return text
}

fun String.toMorse(): String {
	// Criar uma string vazia para guardar a nossa mensagem em morse
	var morse = ""

	// Fazer um for na nossa mensagem
	this.toCharArray().forEach { char ->
		// Pegar o valor do char em morse
		val inMorse = morseValues[char]

		if (inMorse != null) { // E, caso seja diferente de null...
			morse += "$inMorse " // Pegar o nosso valor e colocar na nossa string!
		}
	}
	return morse
}