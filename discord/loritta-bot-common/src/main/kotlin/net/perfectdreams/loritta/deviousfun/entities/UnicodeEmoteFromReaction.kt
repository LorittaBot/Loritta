package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.DiscordPartialEmoji
import net.perfectdreams.loritta.deviousfun.DeviousFun

class UnicodeEmoteFromReaction(
    override val deviousFun: DeviousFun,
    val emoji: DiscordPartialEmoji
) : Emote {
    override val name: String
        get() = emoji.name!!
    override val asMention: String
        get() = emoji.name!!
}