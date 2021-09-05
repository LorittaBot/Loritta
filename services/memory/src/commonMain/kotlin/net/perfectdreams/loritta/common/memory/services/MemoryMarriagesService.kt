package net.perfectdreams.loritta.common.memory.services

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.loritta.common.entities.Marriage
import net.perfectdreams.loritta.common.services.MarriagesService

class MemoryMarriagesService : MarriagesService {
    private val marriages = mutableMapOf<Long, Marriage>()
    // This is required because Kotlin "Common" does not have a "ConcurrentHashMap"
    // So what we need to do is lock all map accesses
    private val accessMutex = Mutex()

    /* init {
        marriages[0L] = MemoryMarriage(
            0L,
            123170274651668480L,
            297153970613387264L,
            Clock.System.now()
        )
    } */

    override suspend fun getMarriageByUser(userId: Long): Marriage? = mapAccess {
        marriages.values.firstOrNull {
            it.user1 == userId || it.user2 == userId
        }
    }

    suspend fun <T> mapAccess(block: (Map<Long, Marriage>) -> (T)): T = accessMutex.withLock {
        block.invoke(marriages)
    }
}