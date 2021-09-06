package net.perfectdreams.loritta.common.services

import net.perfectdreams.loritta.common.entities.ServerConfigRoot

interface ServerConfigsService {
    suspend fun getServerConfigRootById(id: ULong): ServerConfigRoot?
}