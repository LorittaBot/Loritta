package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
class DiscordUser(
        val id: Long,
        val name: String,
        val discriminator: String,
        val avatarUrl: String
)