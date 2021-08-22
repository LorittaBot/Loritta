package net.perfectdreams.loritta.common.pudding.services

import net.perfectdreams.loritta.common.entities.UserProfile
import net.perfectdreams.loritta.common.pudding.entities.PuddingUserProfile
import net.perfectdreams.loritta.common.services.UserService
import net.perfectdreams.pudding.client.PuddingClient

class PuddingUserService(val puddingClient: PuddingClient) : UserService {
    override suspend fun getOrCreateUserProfileById(id: Long): UserProfile {
        return PuddingUserProfile(puddingClient.users.getOrCreateUserProfileById(id))
    }

    override suspend fun getUserProfileById(id: Long): UserProfile? {
        return puddingClient.users.getUserProfileById(id)?.let { PuddingUserProfile(it) }
    }
}