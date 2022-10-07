package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.DiscordEmoji
import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.deviousfun.JDA
import net.perfectdreams.loritta.deviousfun.cache.DeviousGuildEmojiData

class UnicodeEmote(
    override val jda: JDA,
    val emoji: DiscordEmoji
) : Emote {
    override val name: String
        get() = emoji.name!!
    override val asMention: String
        get() = emoji.name!!
}