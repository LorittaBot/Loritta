package net.perfectdreams.loritta.common.emojis

sealed class LorittaEmojiReference {
    data class ApplicationEmoji(val name: String) : LorittaEmojiReference()
    data class GuildEmoji(val name: String, val id: Long, val animated: Boolean) : LorittaEmojiReference()
    data class UnicodeEmoji(val name: String) : LorittaEmojiReference()
}