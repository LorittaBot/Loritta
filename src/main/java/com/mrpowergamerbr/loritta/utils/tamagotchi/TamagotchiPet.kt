package com.mrpowergamerbr.loritta.utils.tamagotchi

class TamagotchiPet(
		val petName: String,
		val gender: PetGender,
		val petType: String
) {
	var lastUpdate: Long = System.currentTimeMillis()
	var hunger: Float = 1f
	var happiness: Float  = 1f
	var hygiene: Float = 1f
	var bladder: Float = 1f
	var social: Float = 1f
	var upgrades = mutableSetOf<PetUpgrades>()

	constructor() : this("???", PetGender.MALE, "???")

	enum class PetGender {
		MALE, FEMALE
	}

	enum class PetUpgrades {
		FAN_ART,
		TELEVISION,
		ANTENNA,
		COUCH,
		MINIBAR,
		LOURO,
		CELLPHONE,
		COMPUTER
	}
}