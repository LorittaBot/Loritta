package net.perfectdreams.loritta.common.pudding.requests

import net.perfectdreams.loritta.common.requests.UserProfileRequestAction
import net.perfectdreams.loritta.common.pudding.services.PuddingUserProfileService
import net.perfectdreams.loritta.common.pudding.entities.PuddingUserProfile

class PuddingUserProfileRequestAction(
    private val service: PuddingUserProfileService,
    private val id: Long
) : UserProfileRequestAction {
    override suspend fun retrieve() = service.puddingClient.users.userProfile(id).retrieve()
        ?.let { PuddingUserProfile(it) }

    override suspend fun retrieveOrCreate() = PuddingUserProfile(service.puddingClient.users.userProfile(id).retrieveOrCreate())
}