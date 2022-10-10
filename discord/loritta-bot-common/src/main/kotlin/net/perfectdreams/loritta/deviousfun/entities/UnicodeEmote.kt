package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.DiscordEmoji
import net.perfectdreams.loritta.deviousfun.DeviousFun

class UnicodeEmote(
    override val deviousFun: DeviousFun,
    val emoji: DiscordEmoji
) : Emote {
    override val name: String
        get() = emoji.name!!
    override val asMention: String
        get() = emoji.name!!
}