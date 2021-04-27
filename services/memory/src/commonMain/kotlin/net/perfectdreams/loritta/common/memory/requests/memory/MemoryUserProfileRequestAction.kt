package net.perfectdreams.loritta.common.memory.requests.memory

import net.perfectdreams.loritta.common.pudding.entities.UserProfile
import net.perfectdreams.loritta.common.memory.requests.UserProfileRequestAction
import net.perfectdreams.loritta.common.memory.services.memory.MemoryUserProfileService

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