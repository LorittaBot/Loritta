package net.perfectdreams.loritta.cinnamon.common.services

import net.perfectdreams.loritta.cinnamon.common.entities.ShipEffect

interface ShipEffectsService {
    suspend fun getShipEffectsForUser(userId: Long): List<ShipEffect>
}