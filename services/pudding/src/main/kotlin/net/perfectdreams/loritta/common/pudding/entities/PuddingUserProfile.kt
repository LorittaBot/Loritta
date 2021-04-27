package net.perfectdreams.loritta.common.pudding.entities

class PuddingUserProfile(private val pudding: net.perfectdreams.loritta.pudding.common.data.UserProfile) : UserProfile {
    override val id by pudding::id
    override val money by pudding::money
}