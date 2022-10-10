package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.DiscordPartialEmoji
import net.perfectdreams.loritta.deviousfun.DeviousFun

class GuildEmoteFromReaction(
    override val deviousFun: DeviousFun,
    val emoji: DiscordPartialEmoji
) : DiscordEmote {
    override val idSnowflake
        get() = emoji.id!!
    override val name: String
        get() = emoji.name!!
    override val isAnimated: Boolean
        get() = emoji.animated.discordBoolean
    override val imageUrl: String
        get() = TODO()
}