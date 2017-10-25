package com.mrpowergamerbr.loritta.utils

import lombok.Getter

@Getter
enum class Jankenpon
	private constructor(var lang: String, var emoji: String, var wins: String, var loses: String) {
	ROCK("pedra", "\uD83C\uDF11", "SCISSORS", "PAPER"),
	PAPER("papel", ":newspaper:", "ROCK", "SCISSORS"),
	SCISSORS("tesoura", ":scissors:", "PAPER", "ROCK");

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
		fun getFromLangString(str: String): Jankenpon? {
			for (janken in Jankenpon.values()) {
				if (janken.lang == str) {
					return janken
				}
			}
			return null
		}
	}
}