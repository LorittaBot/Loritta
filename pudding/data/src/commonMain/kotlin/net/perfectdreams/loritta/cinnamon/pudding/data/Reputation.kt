package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.serialization.Serializable

@Serializable
data class Reputation(
    val id: Long,
    val givenById: Long,
    val givenByIp: String,
    val givenByEmail: String,
    val receivedById: Long,
    val receivedAt: Long,
    val content: String?
)