package net.perfectdreams.loritta.morenitta.utils.locale

import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.Gender

fun Gender.getPersonalPronoun(locale: BaseLocale, type: PersonalPronoun, firstName: String): String {
	return when (this) {
		Gender.MALE -> locale["loritta.pronoun.he"]
		Gender.FEMALE -> locale["loritta.pronoun.she"]
		Gender.UNKNOWN -> firstName
	}
}

fun Gender.getPossessivePronoun(locale: BaseLocale, type: PersonalPronoun, firstName: String): String {
	return when (this) {
		Gender.MALE -> locale["loritta.pronoun.his"]
		Gender.FEMALE -> locale["loritta.pronoun.her"]
		Gender.UNKNOWN -> firstName
	}
}

fun Gender.getPronoun(locale: BaseLocale): String {
	return when (this) {
		Gender.MALE -> "o"
		Gender.FEMALE -> "a"
		Gender.UNKNOWN -> "o"
	}
}