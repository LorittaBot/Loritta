package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale

enum class Jankenpon(var lang: String, var emoji: String, var wins: String, var loses: String) {
	// Os wins e os loses precisam ser uma string já que os enums ainda não foram inicializados
	ROCK("commands.fun.rockpaperscissors.rock", "\uD83C\uDF11", "SCISSORS", "PAPER"),
	PAPER("commands.fun.rockpaperscissors.paper", ":newspaper:", "ROCK", "SCISSORS"),
	SCISSORS("commands.fun.rockpaperscissors.scissors", ":scissors:", "PAPER", "ROCK");

	fun getStatus(janken: Jankenpon): JankenponStatus {
		if (this.name.equals(janken.loses, ignoreCase = true)) {
			return JankenponStatus.WIN
		}
		if (this == janken) {
			return JankenponStatus.DRAW
		}
		return JankenponStatus.LOSE
	}

	enum class JankenponStatus {
		WIN,
		LOSE,
		DRAW
	}

	companion object {
		fun getFromLangString(str: String, locale: LegacyBaseLocale): Jankenpon? {
			for (janken in Jankenpon.values()) {
				if (str == locale.toNewLocale()[janken.lang]) {
					return janken
				}
			}
			return null
		}
	}
}