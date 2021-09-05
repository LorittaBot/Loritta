package net.perfectdreams.loritta.common.services

import net.perfectdreams.loritta.common.entities.ShipEffect

interface ShipEffectsService {
    suspend fun getShipEffectsForUser(userId: Long): List<ShipEffect>
}