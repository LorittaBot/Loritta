package net.perfectdreams.loritta.cinnamon.common.pudding.services

import net.perfectdreams.loritta.cinnamon.common.entities.Marriage
import net.perfectdreams.loritta.cinnamon.common.pudding.entities.PuddingMarriage
import net.perfectdreams.loritta.cinnamon.common.services.MarriagesService
import net.perfectdreams.pudding.client.PuddingClient

class PuddingMarriagesService(val puddingClient: PuddingClient) : MarriagesService {
    override suspend fun getMarriageByUser(userId: Long): Marriage? {
        return puddingClient.marriages.getMarriageByUser(userId)?.let { PuddingMarriage(it) }
    }
}