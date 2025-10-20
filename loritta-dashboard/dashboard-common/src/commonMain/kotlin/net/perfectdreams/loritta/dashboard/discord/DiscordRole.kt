package net.perfectdreams.loritta.dashboard.discord

import kotlinx.serialization.Serializable

@Serializable
data class DiscordRole(
    val id: Long,
    val name: String,
    val color: Int
)