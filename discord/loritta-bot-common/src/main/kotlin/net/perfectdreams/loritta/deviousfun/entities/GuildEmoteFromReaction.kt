package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.deviousfun.JDA
import net.perfectdreams.loritta.deviousfun.cache.DeviousGuildEmojiData

class GuildEmoteFromReaction(
    override val jda: JDA,
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