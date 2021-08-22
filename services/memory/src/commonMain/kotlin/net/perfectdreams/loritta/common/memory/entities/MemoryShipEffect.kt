package net.perfectdreams.loritta.common.memory.entities

import kotlinx.datetime.Instant
import net.perfectdreams.loritta.common.entities.ShipEffect

class MemoryShipEffect(
    override val id: Long,
    override val buyerId: Long,
    override val user1: Long,
    override val user2: Long,
    override val editedShipValue: Int,
    override val expiresAt: Instant
) : ShipEffect