package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

enum class Jankenpon(var lang: String, var emoji: String, var wins: String, var loses: String) {
	// Os wins e os loses precisam ser uma string já que os enums ainda não foram inicializados
	ROCK("PPT_Rock", "\uD83C\uDF11", "SCISSORS", "PAPER"),
	PAPER("PPT_Paper", ":newspaper:", "ROCK", "SCISSORS"),
	SCISSORS("PPT_Scissors", ":scissors:", "PAPER", "ROCK");

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
		fun getFromLangString(str: String, locale: BaseLocale): Jankenpon? {
			for (janken in Jankenpon.values()) {
				if (str == locale[janken.lang]) {
					return janken
				}
			}
			return null
		}
	}
}