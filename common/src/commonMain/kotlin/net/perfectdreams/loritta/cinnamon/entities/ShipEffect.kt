package net.perfectdreams.loritta.cinnamon.entities

import kotlinx.datetime.Instant

interface ShipEffect {
    val id: Long
    val buyerId: Long
    val user1: Long
    val user2: Long
    val editedShipValue: Int
    val expiresAt: Instant
}