package net.perfectdreams.loritta.cinnamon.common.memory.services

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.loritta.cinnamon.common.entities.ShipEffect
import net.perfectdreams.loritta.cinnamon.common.services.ShipEffectsService

class MemoryShipEffectsService : ShipEffectsService {
    private val shipEffects = mutableMapOf<Long, ShipEffect>()
    // This is required because Kotlin "Common" does not have a "ConcurrentHashMap"
    // So what we need to do is lock all profile accesses
    private val accessMutex = Mutex()

    /* init {
        shipEffects[0L] = MemoryShipEffect(
            0L,
            123170274651668480L,
            351760430991147010L,
            236167700777271297L,
            66,
            Clock.System.now().plus(30, DateTimeUnit.HOUR)
        )
    } */

    override suspend fun getShipEffectsForUser(userId: Long): List<ShipEffect> = mapAccess {
        shipEffects.values.filter {
            it.user1 == userId || it.user2 == userId
        }
    }

    suspend fun <T> mapAccess(block: (Map<Long, ShipEffect>) -> (T)): T = accessMutex.withLock {
        block.invoke(shipEffects)
    }
}