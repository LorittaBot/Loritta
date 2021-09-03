package net.perfectdreams.loritta.common.pudding.services

import net.perfectdreams.loritta.common.entities.ShipEffect
import net.perfectdreams.loritta.common.pudding.entities.PuddingShipEffect
import net.perfectdreams.loritta.common.services.ShipEffectsService
import net.perfectdreams.pudding.client.PuddingClient

class PuddingShipEffectsService(val puddingClient: PuddingClient) : ShipEffectsService {
    override suspend fun getShipEffectsForUser(userId: Long): List<ShipEffect> {
        return puddingClient.shipEffects.getShipEffectsForUser(userId).map {
            PuddingShipEffect(it)
        }
    }
}