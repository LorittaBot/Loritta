package net.perfectdreams.loritta.common.services

import net.perfectdreams.loritta.common.entities.UserProfile

interface UserService {
    /**
     * Gets or create a [UserProfile]
     *
     * @param  id the profile's ID
     * @return the user profile
     */
    suspend fun getOrCreateUserProfileById(id: Long): UserProfile

    /**
     * Gets a [UserProfile], if the profile doesn't exist, then null is returned
     *
     * @param id the profile's ID
     * @return the user profile or null if it doesn't exist
     */
    suspend fun getUserProfileById(id: Long): UserProfile?
}