package com.mrpowergamerbr.loritta.utils

import java.net.URLEncoder
import java.text.MessageFormat

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

fun String.escapeMentions(): String {
	return this.replace(Regex("\\\\+@"), "@").replace("@", "@\u200B")
}