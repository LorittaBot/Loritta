package net.perfectdreams.loritta.common.requests.memory

import net.perfectdreams.loritta.common.entities.UserProfile
import net.perfectdreams.loritta.common.requests.UserProfileRequestAction
import net.perfectdreams.loritta.common.services.memory.MemoryUserProfileService

class MemoryUserProfileRequestAction(
    private val service: MemoryUserProfileService,
    private val id: Long
) : UserProfileRequestAction {
    override suspend fun retrieve() = service.profileAccess {
        it[id]
    }

    override suspend fun retrieveOrCreate(): UserProfile {
        TODO("Not yet implemented")
    }
}