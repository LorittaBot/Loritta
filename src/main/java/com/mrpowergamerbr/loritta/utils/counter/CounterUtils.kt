package com.mrpowergamerbr.loritta.utils.counter

object CounterUtils {
	fun generatePrettyCounter(count: Int, theme: CounterThemeName, padding: Int): String {
		return generatePrettyCounter(count, getEmojis(theme), padding)
	}

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

	fun getEmojis(theme: CounterThemeName): List<String> {
		return when (theme) {
			CounterThemeName.RED -> CounterThemes.RED
			CounterThemeName.GREEN -> CounterThemes.GREEN
			CounterThemeName.BLURPLE -> CounterThemes.BLURPLE
			CounterThemeName.BLACK -> CounterThemes.BLACK
			CounterThemeName.DELUXE -> CounterThemes.DELUXE
			CounterThemeName.LORITTA -> CounterThemes.LORITTA
			else -> CounterThemes.DEFAULT
		}
	}
}