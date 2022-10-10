package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.deviousfun.DeviousFun

class ReactionEmote(val deviousFun: DeviousFun, val partialEmoji: DiscordPartialEmoji) : IdentifiableSnowflake {
    override val idSnowflake: Snowflake
        get() = partialEmoji.id ?: error("This is not a custom emoji!")
    val name: String
        get() = partialEmoji.name ?: error("This data is missing, probably because it was in a reaction emoji object!")
    val emote: Emote
        get() = if (isEmote)
            GuildEmoteFromReaction(deviousFun, partialEmoji)
        else
            UnicodeEmoteFromReaction(deviousFun, partialEmoji)
    val isEmote: Boolean
        get() = partialEmoji.id != null

    /**
     * Hacky workaround for JDA v4 support
     */
    fun isEmote(id: String): Boolean {
        return if (this.isEmote)
            this.id == id || this.name == id
        else
            this.name == id
    }
}