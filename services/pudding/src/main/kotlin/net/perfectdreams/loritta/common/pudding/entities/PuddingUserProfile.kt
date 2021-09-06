package net.perfectdreams.loritta.common.pudding.entities

import net.perfectdreams.loritta.common.entities.UserProfile

class PuddingUserProfile(private val pudding: net.perfectdreams.loritta.pudding.common.data.UserProfile) : UserProfile {
    override val id = pudding.id.toULong()
    override val money by pudding::money
}