package net.perfectdreams.loritta.platform.discord.utils

import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.utils.Emotes

object UserFlagBadgeEmotes {

    @JvmStatic
    val repository = hashMapOf(
            User.UserFlag.STAFF to Emotes.DISCORD_STAFF,
            User.UserFlag.PARTNER to Emotes.DISCORD_PARTNER,
            User.UserFlag.VERIFIED_DEVELOPER to Emotes.VERIFIED_DEVELOPER,
            User.UserFlag.HYPESQUAD to Emotes.HYPESQUAD_EVENTS,
            User.UserFlag.EARLY_SUPPORTER to Emotes.EARLY_SUPPORTER,
            User.UserFlag.HYPESQUAD_BRAVERY to Emotes.BRAVERY_HOUSE,
            User.UserFlag.HYPESQUAD_BRILLIANCE to Emotes.BRILLIANCE_HOUSE,
            User.UserFlag.HYPESQUAD_BALANCE to Emotes.BALANCE_HOUSE,
            User.UserFlag.BUG_HUNTER_LEVEL_1 to Emotes.BUG_HUNTER_1,
            User.UserFlag.BUG_HUNTER_LEVEL_2 to Emotes.BUG_HUNTER_2
    ).toMutableMap()

    fun getBadges(user: User): List<String> = user.flags.mapNotNull { flag ->
        repository[flag]?.asMention
    }

}