package net.perfectdreams.loritta.api.utils

import kotlinx.serialization.Serializable

@Serializable
enum class Rarity {
	COMMON,
	UNCOMMON,
	RARE,
	EPIC,
	LEGENDARY;

	fun getProfilePrice() = when (this) {
		COMMON -> 6_000 // 900 * 3
		UNCOMMON -> 12_500 // 900 * 14
		RARE -> 25_000 // 900 * 45
		EPIC -> 60_000 // 900 * 90
		LEGENDARY -> 150_000 // 900 * 120
	}

	fun getBackgroundPrice() = when (this) {
		COMMON -> 2_500 // 900 * 1
		UNCOMMON -> 5_000 // 900 * 3
		RARE -> 10_000 // 900 * 14
		EPIC -> 25_000
		LEGENDARY -> 80_000
	}

	fun getBadgePrice() = when (this) {
		COMMON -> 1_800
		UNCOMMON -> 3_000
		RARE -> 5_000
		EPIC -> 10_000
		LEGENDARY -> 30_000
	}
}