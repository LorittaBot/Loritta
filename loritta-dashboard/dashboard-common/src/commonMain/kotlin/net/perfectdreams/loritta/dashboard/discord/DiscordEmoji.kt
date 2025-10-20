package net.perfectdreams.loritta.dashboard.discord

import kotlinx.serialization.Serializable

@Serializable
data class DiscordEmoji(
    val id: Long,
    val name: String,
    val animated: Boolean
)