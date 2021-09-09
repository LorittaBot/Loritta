package net.perfectdreams.loritta.cinnamon.common.memory.services

import kotlinx.coroutines.sync.Mutex
import net.perfectdreams.loritta.cinnamon.common.services.ServerConfigsService

class MemoryServerConfigsService : ServerConfigsService {
    // This is required because Kotlin "Common" does not have a "ConcurrentHashMap"
    // So what we need to do is lock all map accesses
    private val accessMutex = Mutex()

    override suspend fun getServerConfigRootById(id: ULong) = null
}