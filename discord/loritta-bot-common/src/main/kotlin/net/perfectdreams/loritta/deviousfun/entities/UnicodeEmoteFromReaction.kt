package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.DiscordPartialEmoji
import net.perfectdreams.loritta.deviousfun.JDA

class UnicodeEmoteFromReaction(
    override val jda: JDA,
    val emoji: DiscordPartialEmoji
) : Emote {
    override val name: String
        get() = emoji.name!!
    override val asMention: String
        get() = emoji.name!!
}