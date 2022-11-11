package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.DiscordEmoji
import net.perfectdreams.loritta.deviousfun.DeviousShard

class UnicodeEmote(
    override val deviousShard: DeviousShard,
    val emoji: DiscordEmoji
) : Emote {
    override val name: String
        get() = emoji.name!!
    override val asMention: String
        get() = emoji.name!!
}