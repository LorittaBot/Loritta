package net.perfectdreams.loritta.morenitta.platform.discord.utils

import dev.kord.common.entity.UserFlag
import net.perfectdreams.loritta.deviousfun.entities.User
import net.perfectdreams.loritta.common.utils.Emotes

object UserFlagBadgeEmotes {
    @JvmStatic
    val repository = hashMapOf(
        // UserFlag.STAFF to Emotes.DISCORD_STAFF,
        UserFlag.DiscordPartner to Emotes.DISCORD_PARTNER,
        UserFlag.VerifiedBotDeveloper to Emotes.VERIFIED_DEVELOPER,
        UserFlag.HypeSquad to Emotes.HYPESQUAD_EVENTS,
        UserFlag.EarlySupporter to Emotes.EARLY_SUPPORTER,
        UserFlag.HouseBravery to Emotes.BRAVERY_HOUSE,
        UserFlag.HouseBrilliance to Emotes.BRILLIANCE_HOUSE,
        UserFlag.HouseBalance to Emotes.BALANCE_HOUSE,
        UserFlag.BugHunterLevel1 to Emotes.BUG_HUNTER_1,
        UserFlag.BugHunterLevel2 to Emotes.BUG_HUNTER_2
    ).toMutableMap()

    fun getBadges(user: User): List<String> = user.flags.flags.mapNotNull { flag ->
        repository[flag]?.asMention
    }

}