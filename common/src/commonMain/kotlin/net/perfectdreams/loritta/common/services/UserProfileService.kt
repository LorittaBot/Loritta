package net.perfectdreams.loritta.common.services

import net.perfectdreams.loritta.common.entities.UserProfile
import net.perfectdreams.loritta.common.requests.UserProfileRequestAction

interface UserProfileService {
    /**
     * Initializes a request action to retrieve a [UserProfile]
     *
     * @param id the user ID
     * @return the request action
     */
    fun id(id: Long): UserProfileRequestAction

    // TODO: list with filter call
}