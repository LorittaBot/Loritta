package net.perfectdreams.loritta.cinnamon.common.pudding.services

import net.perfectdreams.loritta.cinnamon.common.entities.ShipEffect
import net.perfectdreams.loritta.cinnamon.common.pudding.entities.PuddingShipEffect
import net.perfectdreams.loritta.cinnamon.common.services.ShipEffectsService
import net.perfectdreams.pudding.client.PuddingClient

class PuddingShipEffectsService(val puddingClient: PuddingClient) : ShipEffectsService {
    override suspend fun getShipEffectsForUser(userId: Long): List<ShipEffect> {
        return puddingClient.shipEffects.getShipEffectsForUser(userId).map {
            PuddingShipEffect(it)
        }
    }
}