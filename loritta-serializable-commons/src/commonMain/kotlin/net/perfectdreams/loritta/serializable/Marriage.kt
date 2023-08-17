package net.perfectdreams.loritta.serializable

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Marriage(
    val id: Long,
    val user1: UserId,
    val user2: UserId,
    val marriedSince: Instant
)