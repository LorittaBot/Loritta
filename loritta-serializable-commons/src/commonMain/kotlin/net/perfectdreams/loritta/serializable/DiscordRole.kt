package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
data class DiscordRole(
    val id: Long,
    val name: String,
    val color: Int
)