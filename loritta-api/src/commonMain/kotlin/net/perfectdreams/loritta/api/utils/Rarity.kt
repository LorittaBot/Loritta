package net.perfectdreams.loritta.api.utils

enum class Rarity {
	COMMON,
	UNCOMMON,
	RARE,
	EPIC,
	LEGENDARY;

	fun getProfilePrice() = when (this) {
		COMMON -> 3_500
		UNCOMMON -> 8_500
		RARE -> 35_000
		EPIC -> 60_000
		LEGENDARY -> 100_000
	}

	fun getBackgroundPrice() = when (this) {
		COMMON -> 1_000
		UNCOMMON -> 3_500
		RARE -> 15_000
		EPIC -> 35_000
		LEGENDARY -> 70_000
	}

	fun getBadgePrice() = when (this) {
		COMMON -> 500
		UNCOMMON -> 1_500
		RARE -> 3_500
		EPIC -> 7_000
		LEGENDARY -> 15_000
	}
}