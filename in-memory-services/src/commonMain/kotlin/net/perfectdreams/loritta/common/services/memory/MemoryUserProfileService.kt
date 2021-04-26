package net.perfectdreams.loritta.common.services.memory

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.loritta.common.entities.UserProfile
import net.perfectdreams.loritta.common.requests.UserProfileRequestAction
import net.perfectdreams.loritta.common.requests.memory.MemoryUserProfileRequestAction
import net.perfectdreams.loritta.common.services.UserProfileService

class MemoryUserProfileService : UserProfileService {
    private val profiles = mutableMapOf<Long, UserProfile>()
    // This is required because Kotlin "Common" does not have a "ConcurrentHashMap"
    // So what we need to do is lock all profile accesses
    private val profileAccessMutex = Mutex()

    override fun id(id: Long) = MemoryUserProfileRequestAction(this, id)

    internal suspend fun <T> profileAccess(block: (Map<Long, UserProfile>) -> (T)): T = profileAccessMutex.withLock {
        block.invoke(profiles)
    }
}