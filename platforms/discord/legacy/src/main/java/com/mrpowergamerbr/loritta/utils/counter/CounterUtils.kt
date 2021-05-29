package com.mrpowergamerbr.loritta.utils.counter

object CounterUtils {

    fun generatePrettyCounter(count: Int, list: List<String>, padding: Int): String {
		var counter = ""

		for (char in count.toString()) {
			val emote = list[char.toString().toInt()]

			counter += emote
		}

		val paddingCount = padding - count.toString().length

		if (paddingCount > 0) {
			for (i in 0 until paddingCount) {
				counter = list[0] + counter
			}
		}

		return counter
	}

	fun getEmojis(theme: CounterThemes): List<String> {
		return theme.emotes ?: throw UnsupportedOperationException("Theme ${theme.name} doesn't have emotes!")
	}
}