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
		registerDesign(NostalgiaProfileCreator.NostalgiaDarkProfileCreator(m))
		registerDesign(NostalgiaProfileCreator.NostalgiaBlurpleProfileCreator(m))
		registerDesign(NostalgiaProfileCreator.NostalgiaRedProfileCreator(m))
		registerDesign(NostalgiaProfileCreator.NostalgiaBlueProfileCreator(m))
		registerDesign(NostalgiaProfileCreator.NostalgiaGreenProfileCreator(m))
		registerDesign(NostalgiaProfileCreator.NostalgiaPurpleProfileCreator(m))
		registerDesign(NostalgiaProfileCreator.NostalgiaPinkProfileCreator(m))
		registerDesign(NostalgiaProfileCreator.NostalgiaOrangeProfileCreator(m))
		registerDesign(NostalgiaProfileCreator.NostalgiaYellowProfileCreator(m))
		registerDesign(DebugProfileCreator())
		registerDesign(DefaultProfileCreator(m))
		registerDesign(MSNProfileCreator(m))
		registerDesign(OrkutProfileCreator(m))
		registerDesign(PlainProfileCreator.PlainWhiteProfileCreator(m))
		registerDesign(PlainProfileCreator.PlainOrangeProfileCreator(m))
		registerDesign(PlainProfileCreator.PlainPurpleProfileCreator(m))
		registerDesign(PlainProfileCreator.PlainAquaProfileCreator(m))
		registerDesign(PlainProfileCreator.PlainGreenProfileCreator(m))
		registerDesign(PlainProfileCreator.PlainGreenHeartsProfileCreator(m))
		registerDesign(CowboyProfileCreator(m))
		registerDesign(NextGenProfileCreator(m))
		registerDesign(MonicaAtaProfileCreator(m))
		registerDesign(UndertaleProfileCreator(m))
		registerDesign(LoriAtaProfileCreator(m))
		registerDesign(Halloween2019ProfileCreator(m))
		registerDesign(Christmas2019ProfileCreator(m))
		registerDesign(LorittaChristmas2019ProfileCreator(m))
		
		// ===[ DISCORD USER FLAGS BADGES ]===
		registerBadge(DiscordUserFlagBadge.DiscordStaffBadge())
		registerBadge(DiscordUserFlagBadge.DiscordPartnerBadge())
		registerBadge(DiscordUserFlagBadge.DiscordVerifiedDeveloperBadge())
		registerBadge(DiscordUserFlagBadge.DiscordHypesquadEventsBadge())
		registerBadge(DiscordUserFlagBadge.DiscordEarlySupporterBadge())
		registerBadge(DiscordUserFlagBadge.DiscordBraveryHouseBadge())
		registerBadge(DiscordUserFlagBadge.DiscordBrillanceHouseBadge())
		registerBadge(DiscordUserFlagBadge.DiscordBalanceHouseBadge())

		registerBadge(DiscordNitroBadge(m))

		registerBadge(ArtistBadge(m))

		registerBadge(CanecaBadge(m.config.quirky))
		registerBadge(HalloweenBadge(m))
		registerBadge(ChristmasBadge(m))
		registerBadge(GabrielaBadge(m))
		registerBadge(PantufaBadge(m))
	}
}