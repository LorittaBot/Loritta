package net.perfectdreams.loritta.cinnamon.common.services

import net.perfectdreams.loritta.cinnamon.common.entities.UserProfile

interface UserService {
    /**
     * Gets or create a [UserProfile]
     *
     * @param  id the profile's ID
     * @return the user profile
     */
    suspend fun getOrCreateUserProfileById(id: ULong): UserProfile

    /**
     * Gets a [UserProfile], if the profile doesn't exist, then null is returned
     *
     * @param id the profile's ID
     * @return the user profile or null if it doesn't exist
     */
    suspend fun getUserProfileById(id: ULong): UserProfile?
}