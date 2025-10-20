package net.perfectdreams.loritta.dashboard.discord

import kotlinx.serialization.Serializable

@Serializable
data class DiscordUser(
    val id: Long,
    val name: String,
    val globalName: String?,
    val discriminator: String,
    val avatarId: String?
)