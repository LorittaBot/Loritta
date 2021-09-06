package net.perfectdreams.loritta.common.pudding.services

import net.perfectdreams.loritta.common.entities.UserProfile
import net.perfectdreams.loritta.common.pudding.entities.PuddingUserProfile
import net.perfectdreams.loritta.common.services.UserService
import net.perfectdreams.pudding.client.PuddingClient

class PuddingUserService(val puddingClient: PuddingClient) : UserService {
    override suspend fun getOrCreateUserProfileById(id: ULong): UserProfile {
        return PuddingUserProfile(puddingClient.users.getOrCreateUserProfileById(id.toLong()))
    }

    override suspend fun getUserProfileById(id: ULong): UserProfile? {
        return puddingClient.users.getUserProfileById(id.toLong())?.let { PuddingUserProfile(it) }
    }
}