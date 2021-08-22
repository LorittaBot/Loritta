package net.perfectdreams.loritta.common.memory.services

import net.perfectdreams.loritta.common.services.SonhosService

class MemorySonhosService(val userService: MemoryUserService) : SonhosService {
    override suspend fun getSonhosRankPositionBySonhos(sonhos: Long): Long {
        return userService.mapAccess {
            it.values
                .asSequence()
                .filter { it.money >= sonhos }
                .sortedByDescending { it.money }
                .count()
                .toLong()
        }
    }
}