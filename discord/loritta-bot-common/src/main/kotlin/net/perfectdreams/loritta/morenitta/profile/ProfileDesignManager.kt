package net.perfectdreams.loritta.morenitta.profile

import net.perfectdreams.loritta.morenitta.profile.badges.*
import net.perfectdreams.loritta.morenitta.LorittaBot

class ProfileDesignManager(val m: LorittaBot) {
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
		registerDesign(DebugProfileCreator())
		registerDesign(DefaultProfileCreator())
		registerDesign(MSNProfileCreator())
		registerDesign(OrkutProfileCreator())
		registerDesign(PlainProfileCreator.PlainWhiteProfileCreator())
		registerDesign(PlainProfileCreator.PlainOrangeProfileCreator())
		registerDesign(PlainProfileCreator.PlainPurpleProfileCreator())
		registerDesign(PlainProfileCreator.PlainAquaProfileCreator())
		registerDesign(PlainProfileCreator.PlainGreenProfileCreator())
		registerDesign(PlainProfileCreator.PlainGreenHeartsProfileCreator())
		registerDesign(CowboyProfileCreator())
		registerDesign(NextGenProfileCreator())
		registerDesign(MonicaAtaProfileCreator())
		registerDesign(UndertaleProfileCreator())
		registerDesign(LoriAtaProfileCreator())
		registerDesign(Halloween2019ProfileCreator())
		registerDesign(Christmas2019ProfileCreator())
		registerDesign(LorittaChristmas2019ProfileCreator())
		
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

		registerBadge(CanecaBadge(m.config.quirky))
		registerBadge(HalloweenBadge())
		registerBadge(ChristmasBadge())
		registerBadge(GabrielaBadge())
		registerBadge(PantufaBadge())
	}
}