package net.perfectdreams.loritta.common.pudding.services

import net.perfectdreams.loritta.common.services.UserProfileService
import net.perfectdreams.loritta.common.pudding.requests.PuddingUserProfileRequestAction
import net.perfectdreams.pudding.client.PuddingClient

class PuddingUserProfileService(val puddingClient: PuddingClient) : UserProfileService {
    override fun id(id: Long) = PuddingUserProfileRequestAction(this, id)
}