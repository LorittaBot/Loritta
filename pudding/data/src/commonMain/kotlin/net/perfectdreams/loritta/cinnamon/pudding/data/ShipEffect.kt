package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ShipEffect(
    val id: Long,
    val buyerId: UserId,
    val user1: UserId,
    val user2: UserId,
    val editedShipValue: Int,
    val expiresAt: Instant
)