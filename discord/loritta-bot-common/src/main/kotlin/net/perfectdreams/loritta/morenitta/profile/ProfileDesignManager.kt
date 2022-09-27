package net.perfectdreams.loritta.morenitta.profile

import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.cinnamon.discord.utils.profiles.StaticProfileCreator
import net.perfectdreams.loritta.morenitta.profile.badges.*
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ProfileDesign

class ProfileDesignManager(val m: LorittaBot) {
	companion object {
		private val FREE_EMOJIS_GUILDS = listOf(
			Snowflake(297732013006389252), // Apartamento da Loritta
			Snowflake(320248230917046282), // SparklyPower
			Snowflake(417061847489839106), // Rede Dark
			Snowflake(769892417025212497), // Kuraminha's House
			Snowflake(769030809159073795)  // Saddest of the Sads
		)
	}

	val designs = mutableListOf<ProfileCreator>()
	val defaultProfileDesign: StaticProfileCreator
		get() = designs.first { it.internalName == ProfileDesign.DEFAULT_PROFILE_DESIGN_ID } as StaticProfileCreator

	val badges = mutableListOf<Badge>()

	fun registerBadge(badge: Badge) {
		badges.add(badge)
	}

	fun registerDesign(design: ProfileCreator) {
		designs.removeIf { it.internalName == design.internalName }
		designs.add(design)
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

		// registerBadge(CanecaBadge(m.config.quirky))
		registerBadge(HalloweenBadge(m))
		registerBadge(ChristmasBadge(m))
		registerBadge(GabrielaBadge(m))
		registerBadge(PantufaBadge(m))
	}
}