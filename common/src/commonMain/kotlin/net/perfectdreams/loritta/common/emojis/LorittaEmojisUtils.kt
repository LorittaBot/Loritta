package net.perfectdreams.loritta.common.emojis

import net.perfectdreams.loritta.common.emotes.DiscordEmote
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.common.emotes.UnicodeEmote

/**
 * Converts a Loritta [Emote] to a [LorittaEmojiReference], used to bridge [Emote] to [LorittaEmojiReference]
 * without requiring to migrate all emotes to the new system
 */
fun Emote.toLorittaEmojiReference(): LorittaEmojiReference {
    return when (this) {
        is DiscordEmote -> LorittaEmojiReference.GuildEmoji(this.name, this.id, this.animated)
        is UnicodeEmote -> LorittaEmojiReference.UnicodeEmoji(this.name)
    }
}