package net.perfectdreams.loritta.common.memory.services

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.loritta.common.entities.UserProfile
import net.perfectdreams.loritta.common.memory.entities.MemoryUserProfile
import net.perfectdreams.loritta.common.services.UserService

class MemoryUserService : UserService {
    private val profiles = mutableMapOf<Long, UserProfile>()
    // This is required because Kotlin "Common" does not have a "ConcurrentHashMap"
    // So what we need to do is lock all map accesses
    private val accessMutex = Mutex()

    init {
        profiles[123170274651668480L] = MemoryUserProfile(
            123170274651668480L,
            150L
        )

        profiles[297153970613387264L] = MemoryUserProfile(
            297153970613387264L,
            1000L
        )
    }

    override suspend fun getOrCreateUserProfileById(id: Long): UserProfile {
        return getUserProfileById(id) ?: run {
            mapAccess {
                val profile = MemoryUserProfile(
                    id,
                    0L
                )
                profiles[id] = profile
                profile
            }
        }
    }

    override suspend fun getUserProfileById(id: Long): UserProfile? {
        return mapAccess {
            profiles[id]
        }
    }

    suspend fun <T> mapAccess(block: (Map<Long, UserProfile>) -> (T)): T = accessMutex.withLock {
        block.invoke(profiles)
    }
}