package com.mrpowergamerbr.loritta.utils.counter

import com.mrpowergamerbr.loritta.utils.Constants

object CounterUtils {
	fun generatePrettyCounter(count: Int, theme: CounterThemeName, padding: Int = 5): String {
		return generatePrettyCounter(count, getEmojis(theme), padding)
	}

	fun generatePrettyCounter(count: Int, list: List<String>, padding: Int = 5): String {
		var counter = ""

		for (char in count.toString()) {
			val emote = list[char.toString().toInt()]

			counter += emote
		}

		return counter.padStart(padding, '-').replace("-", Constants.INDEXES[0])
	}

	fun getEmojis(theme: CounterThemeName): List<String> {
		return when (theme) {
			else -> CounterThemes.DEFAULT
		}
	}
}