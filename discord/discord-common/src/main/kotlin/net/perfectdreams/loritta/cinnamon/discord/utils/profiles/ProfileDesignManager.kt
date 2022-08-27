package net.perfectdreams.loritta.cinnamon.discord.utils.profiles

import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon

class ProfileDesignManager(val loritta: LorittaCinnamon) {
    val designs = mutableListOf<ProfileCreator>()
    val defaultProfileDesign: ProfileCreator
        get() = designs.first { it.internalName == "defaultDark" }

    // val badges = mutableListOf<Badge>()

    // TODO: Fix this
    /* fun registerBadge(badge: Badge) {
        badges.add(badge)
    }

    fun unregisterBadge(badge: Badge) {
        badges.remove(badge)
    } */

    fun registerDesign(design: ProfileCreator) {
        designs.removeIf { it.internalName == design.internalName }
        designs.add(design)
    }

    fun unregisterDesign(design: ProfileCreator) {
        designs.removeIf { it.internalName == design.internalName }
    }

    init {
        registerDesign(NostalgiaProfileCreator.NostalgiaDarkProfileCreator(loritta))
        registerDesign(NostalgiaProfileCreator.NostalgiaBlurpleProfileCreator(loritta))
        registerDesign(NostalgiaProfileCreator.NostalgiaRedProfileCreator(loritta))
        registerDesign(NostalgiaProfileCreator.NostalgiaBlueProfileCreator(loritta))
        registerDesign(NostalgiaProfileCreator.NostalgiaGreenProfileCreator(loritta))
        registerDesign(NostalgiaProfileCreator.NostalgiaPurpleProfileCreator(loritta))
        registerDesign(NostalgiaProfileCreator.NostalgiaPinkProfileCreator(loritta))
        registerDesign(NostalgiaProfileCreator.NostalgiaOrangeProfileCreator(loritta))
        registerDesign(NostalgiaProfileCreator.NostalgiaYellowProfileCreator(loritta))

        // ===[ DISCORD USER FLAGS BADGES ]===
        // TODO: Fix this
        /* registerBadge(DiscordUserFlagBadge.DiscordStaffBadge())
        registerBadge(DiscordUserFlagBadge.DiscordPartnerBadge())
        registerBadge(DiscordUserFlagBadge.DiscordVerifiedDeveloperBadge())
        registerBadge(DiscordUserFlagBadge.DiscordHypesquadEventsBadge())
        registerBadge(DiscordUserFlagBadge.DiscordEarlySupporterBadge())
        registerBadge(DiscordUserFlagBadge.DiscordBraveryHouseBadge())
        registerBadge(DiscordUserFlagBadge.DiscordBrillanceHouseBadge())
        registerBadge(DiscordUserFlagBadge.DiscordBalanceHouseBadge())

        registerBadge(DiscordNitroBadge())

        registerBadge(ArtistBadge()) */
    }
}