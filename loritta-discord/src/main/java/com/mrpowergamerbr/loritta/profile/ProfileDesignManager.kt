package com.mrpowergamerbr.loritta.profile

import net.perfectdreams.loritta.api.utils.Rarity
import net.perfectdreams.loritta.profile.ArtistBadge
import net.perfectdreams.loritta.profile.Badge
import net.perfectdreams.loritta.profile.DiscordNitroBadge
import net.perfectdreams.loritta.profile.DiscordUserFlagBadge

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
				ProfileDesign(true,
						NostalgiaProfileCreator::class.java,
						"default",
						Rarity.COMMON,
						listOf(),
						false,
						false
				)
		)

		// ===[ DISCORD USER FLAGS BADGES ]===
		registerBadge(DiscordUserFlagBadge.DiscordStaffBadge())
		registerBadge(DiscordUserFlagBadge.DiscordPartnerBadge())
		registerBadge(DiscordUserFlagBadge.DiscordVerifiedDeveloperBadge())
		registerBadge(DiscordUserFlagBadge.DiscordHypesquadEventsBadge())
		registerBadge(DiscordUserFlagBadge.DiscordEarlySupporterBadge())
		registerBadge(DiscordUserFlagBadge.DiscordBraveryHouseBadge())
		registerBadge(DiscordUserFlagBadge.DiscordBrillanceHouseBadge())
		registerBadge(DiscordUserFlagBadge.DiscordBalanceHouseBadge())

		registerBadge(
				DiscordNitroBadge()
		)

		registerBadge(
				ArtistBadge()
		)
	}
}