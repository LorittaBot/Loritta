package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
class Daily(
    val user: UserId,
    val receivedAt: Instant
)