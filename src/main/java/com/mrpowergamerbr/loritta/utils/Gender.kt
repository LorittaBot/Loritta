package com.mrpowergamerbr.loritta.utils

enum class Gender {
	MALE,
	FEMALE,
	UNKNOWN;

	fun getValidActionFolderNames(other: Gender): List<String> {
		val folderNames = mutableListOf(
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

		return folderNames
	}
}