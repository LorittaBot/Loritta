package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.deviousfun.DeviousShard

class ReactionEmote(val deviousShard: DeviousShard, val partialEmoji: DiscordPartialEmoji) : IdentifiableSnowflake {
    override val idSnowflake: Snowflake
        get() = partialEmoji.id ?: error("This is not a custom emoji!")
    val name: String
        get() = partialEmoji.name ?: error("This data is missing, probably because it was in a reaction emoji object!")
    val emote: Emote
        get() = if (isEmote)
            GuildEmoteFromReaction(deviousShard, partialEmoji)
        else
            UnicodeEmoteFromReaction(deviousShard, partialEmoji)
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