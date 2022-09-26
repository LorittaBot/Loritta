package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.serialization.Serializable

@Serializable
data class StarboardConfig(
    val enabled: Boolean,
    val starboardChannelId: ULong,
    val requiredStars: Int
)