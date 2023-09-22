package net.perfectdreams.loritta.serializable.config

import kotlinx.serialization.Serializable

@Serializable
data class GuildStarboardConfig(
    val enabled: Boolean,
    val starboardChannelId: Long?,
    val requiredStars: Int
)