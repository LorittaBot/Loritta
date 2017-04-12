package com.mrpowergamerbr.loritta.utils;

import lombok.Getter;

@Getter
public enum Jankenpon {
	ROCK("pedra", ":black_circle:", "SCISSORS", "PAPER"),
	PAPER("papel", ":newspaper:", "ROCK", "SCISSORS"),
	SCISSORS("tesoura", ":scissors:", "PAPER", "ROCK");
	
	String lang;
	String emoji;
	String wins;
	String loses;
	
	Jankenpon(String lang, String emoji, String wins, String loses) {
		this.lang = lang;
		this.emoji = emoji;
		this.wins = wins;
		this.loses = loses;
	}
	
	public JankenponStatus getStatus(Jankenpon janken) {
		if (this.name().equalsIgnoreCase(janken.loses)) {
			return JankenponStatus.WIN;
		}
		if (this == janken) {
			return JankenponStatus.DRAW;
		}
		return JankenponStatus.LOSE;
	}
	
	public static Jankenpon getFromLangString(String str) {
		for (Jankenpon janken : Jankenpon.values()) {
			if (janken.lang.equals(str)) {
				return janken;
			}
		}
		return null;
	}
	public enum JankenponStatus {
		WIN,
		LOSE,
		DRAW;
	}
}
