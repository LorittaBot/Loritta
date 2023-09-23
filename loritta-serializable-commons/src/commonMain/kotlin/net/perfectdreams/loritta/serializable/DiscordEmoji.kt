package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
data class DiscordEmoji(
    val id: Long,
    val name: String,
    val animated: Boolean
)