package net.perfectdreams.loritta.cinnamon.common.pudding.services

import net.perfectdreams.loritta.cinnamon.common.entities.ServerConfigRoot
import net.perfectdreams.loritta.cinnamon.common.pudding.entities.PuddingServerConfigRoot
import net.perfectdreams.loritta.cinnamon.common.services.ServerConfigsService
import net.perfectdreams.pudding.client.PuddingClient

class PuddingServerConfigsService(val puddingClient: PuddingClient) : ServerConfigsService {
    override suspend fun getServerConfigRootById(id: ULong): ServerConfigRoot? {
        return puddingClient.serverConfigs.getServerConfigRootById(id.toLong())?.let { PuddingServerConfigRoot(it) }
    }
}