package net.perfectdreams.loritta.cinnamon.common.pudding.entities

import net.perfectdreams.loritta.cinnamon.common.entities.UserProfile

class PuddingUserProfile(private val pudding: net.perfectdreams.loritta.cinnamon.pudding.common.data.UserProfile) : UserProfile {
    override val id = pudding.id.toULong()
    override val money by pudding::money
}