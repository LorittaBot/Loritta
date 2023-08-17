package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
data class StarboardConfig(
    val enabled: Boolean,
    val starboardChannelId: ULong,
    val requiredStars: Int
)