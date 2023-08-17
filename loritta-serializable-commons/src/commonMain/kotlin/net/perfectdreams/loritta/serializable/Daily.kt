package net.perfectdreams.loritta.serializable

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
class Daily(
    val user: UserId,
    val receivedAt: Instant
)