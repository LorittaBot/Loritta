package net.perfectdreams.loritta.cinnamon.common.services

import net.perfectdreams.loritta.cinnamon.common.entities.ServerConfigRoot

interface ServerConfigsService {
    suspend fun getServerConfigRootById(id: ULong): ServerConfigRoot?
}