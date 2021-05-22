package com.mrpowergamerbr.loritta.utils.locale

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.common.locale.BaseLocale

enum class Gender {
	MALE,
	FEMALE,
	UNKNOWN;

	fun getValidActionFolderNames(other: Gender): Set<String> {
		val folderNames = mutableSetOf(
				Constants.ACTION_GENERIC
		)

		if (this == MALE && other == FEMALE) {
			folderNames.add(Constants.ACTION_MALE_AND_FEMALE)
			folderNames.add(Constants.ACTION_BOTH)
		}

		if (this == FEMALE && other == MALE) {
			folderNames.add(Constants.ACTION_FEMALE_AND_MALE)
			folderNames.add(Constants.ACTION_BOTH)
		}

		if (this == MALE && other == MALE) {
			folderNames.add(Constants.ACTION_MALE_AND_MALE)
		}

		if (this == FEMALE && other == FEMALE) {
			folderNames.add(Constants.ACTION_FEMALE_AND_FEMALE)
		}

		if (this == MALE && other == UNKNOWN) {
			folderNames.add(Constants.ACTION_MALE_AND_FEMALE)
			folderNames.add(Constants.ACTION_MALE_AND_MALE)
			folderNames.add(Constants.ACTION_BOTH)
		}

		if (this == FEMALE && other == UNKNOWN) {
			folderNames.add(Constants.ACTION_FEMALE_AND_MALE)
			folderNames.add(Constants.ACTION_FEMALE_AND_FEMALE)
			folderNames.add(Constants.ACTION_BOTH)
		}

		if (this == UNKNOWN && other == MALE) {
			folderNames.add(Constants.ACTION_MALE_AND_MALE)
			folderNames.add(Constants.ACTION_FEMALE_AND_MALE)
			folderNames.add(Constants.ACTION_BOTH)
		}

		if (this == UNKNOWN && other == FEMALE) {
			folderNames.add(Constants.ACTION_MALE_AND_FEMALE)
			folderNames.add(Constants.ACTION_FEMALE_AND_FEMALE)
			folderNames.add(Constants.ACTION_BOTH)
		}

		if (this == UNKNOWN && other == UNKNOWN) {
			folderNames.add(Constants.ACTION_MALE_AND_MALE)
			folderNames.add(Constants.ACTION_FEMALE_AND_MALE)
			folderNames.add(Constants.ACTION_MALE_AND_FEMALE)
			folderNames.add(Constants.ACTION_FEMALE_AND_FEMALE)
			folderNames.add(Constants.ACTION_BOTH)
		}

		return folderNames
	}

	fun getPersonalPronoun(locale: BaseLocale, type: PersonalPronoun, firstName: String): String {
		return when (this) {
			MALE -> locale["loritta.pronoun.he"]
			FEMALE -> locale["loritta.pronoun.she"]
			UNKNOWN -> firstName
		}
	}

	fun getPossessivePronoun(locale: BaseLocale, type: PersonalPronoun, firstName: String): String {
		return when (this) {
			MALE -> locale["loritta.pronoun.his"]
			FEMALE -> locale["loritta.pronoun.her"]
			UNKNOWN -> firstName
		}
	}

	fun getPronoun(locale: BaseLocale): String {
		return when (this) {
			MALE -> {
				"o"
			}
			FEMALE -> {
				"a"
			}
			UNKNOWN -> {
				"o"
			}
		}
	}
}