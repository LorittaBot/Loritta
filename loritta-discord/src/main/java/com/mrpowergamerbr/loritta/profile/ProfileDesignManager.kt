package com.mrpowergamerbr.loritta.profile

import net.perfectdreams.loritta.profile.ArtistBadge
import net.perfectdreams.loritta.profile.Badge
import net.perfectdreams.loritta.profile.DiscordNitroBadge
import net.perfectdreams.loritta.profile.DiscordUserFlagBadge

class ProfileDesignManager {
	val designs = mutableListOf<ProfileCreator>()
	val badges = mutableListOf<Badge>()

	fun registerBadge(badge: Badge) {
		badges.add(badge)
	}

	fun unregisterBadge(badge: Badge) {
		badges.remove(badge)
	}

	fun registerDesign(design: ProfileCreator) {
		designs.removeIf { it.internalName == design.internalName }
		designs.add(design)
	}

	fun unregisterDesign(design: ProfileCreator) {
		designs.removeIf { it.internalName == design.internalName }
	}

	init {
		registerDesign(NostalgiaProfileCreator.NostalgiaDarkProfileCreator())
		registerDesign(NostalgiaProfileCreator.NostalgiaBlurpleProfileCreator())
		registerDesign(NostalgiaProfileCreator.NostalgiaRedProfileCreator())
		registerDesign(NostalgiaProfileCreator.NostalgiaBlueProfileCreator())
		registerDesign(NostalgiaProfileCreator.NostalgiaGreenProfileCreator())
		registerDesign(NostalgiaProfileCreator.NostalgiaPurpleProfileCreator())
		registerDesign(NostalgiaProfileCreator.NostalgiaPinkProfileCreator())
		registerDesign(NostalgiaProfileCreator.NostalgiaOrangeProfileCreator())
		registerDesign(NostalgiaProfileCreator.NostalgiaYellowProfileCreator())

		// ===[ DISCORD USER FLAGS BADGES ]===
		registerBadge(DiscordUserFlagBadge.DiscordStaffBadge())
		registerBadge(DiscordUserFlagBadge.DiscordPartnerBadge())
		registerBadge(DiscordUserFlagBadge.DiscordVerifiedDeveloperBadge())
		registerBadge(DiscordUserFlagBadge.DiscordHypesquadEventsBadge())
		registerBadge(DiscordUserFlagBadge.DiscordEarlySupporterBadge())
		registerBadge(DiscordUserFlagBadge.DiscordBraveryHouseBadge())
		registerBadge(DiscordUserFlagBadge.DiscordBrillanceHouseBadge())
		registerBadge(DiscordUserFlagBadge.DiscordBalanceHouseBadge())

		registerBadge(DiscordNitroBadge())

		registerBadge(ArtistBadge())
	}
}