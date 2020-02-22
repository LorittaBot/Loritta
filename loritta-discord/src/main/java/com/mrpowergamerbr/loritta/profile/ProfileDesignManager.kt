package com.mrpowergamerbr.loritta.profile

import net.perfectdreams.loritta.profile.*

class ProfileDesignManager {
	val designs = mutableListOf<ProfileDesign>()
	val publicDesigns: List<ProfileDesign>
		get() = designs.filter { it.public }
	val badges = mutableListOf<Badge>()

	fun registerBadge(badge: Badge) {
		badges.add(badge)
	}

	fun unregisterBadge(badge: Badge) {
		badges.remove(badge)
	}

	fun registerDesign(design: ProfileDesign) {
		designs.removeIf { it.internalType == design.internalType }
		designs.add(design)
	}

	fun unregisterDesign(design: ProfileDesign) {
		designs.removeIf { it.internalType == design.internalType }
	}

	init {
		registerDesign(
				ProfileDesign(true, NostalgiaProfileCreator::class.java, "default", 0, listOf())
		)
		registerDesign(
				ProfileDesign(true, DefaultProfileCreator::class.java, "modern", 2000, listOf())
		)
		registerDesign(
				ProfileDesign(true, MSNProfileCreator::class.java, "msn", 7500, listOf())
		)
		registerDesign(
				ProfileDesign(true, OrkutProfileCreator::class.java, "orkut", 7500, listOf())
		)

		registerBadge(
				DiscordHouseBadge.DiscordBraveryHouseBadge()
		)

		registerBadge(
				DiscordHouseBadge.DiscordBrillanceHouseBadge()
		)

		registerBadge(
				DiscordHouseBadge.DiscordBalanceHouseBadge()
		)

		registerBadge(
				DiscordEarlySupporterBadge()
		)

		registerBadge(
				DiscordNitroBadge()
		)

		registerBadge(
				ArtistBadge()
		)
	}
}