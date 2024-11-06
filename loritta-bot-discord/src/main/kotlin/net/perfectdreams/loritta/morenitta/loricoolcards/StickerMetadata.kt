package net.perfectdreams.loritta.morenitta.loricoolcards

import kotlinx.serialization.Serializable

@Serializable
sealed class StickerMetadata {
    @Serializable
    data class DiscordUserStickerMetadata(val userId: Long) : StickerMetadata()
}