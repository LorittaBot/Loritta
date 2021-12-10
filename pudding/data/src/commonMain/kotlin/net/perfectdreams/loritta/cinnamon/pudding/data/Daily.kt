package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.serialization.Serializable

@Serializable
data class Daily(
    val receivedById: Long,
    val receivedAt: Long,
    val ip: String,
    val email: String,
    val userAgent: String?
)