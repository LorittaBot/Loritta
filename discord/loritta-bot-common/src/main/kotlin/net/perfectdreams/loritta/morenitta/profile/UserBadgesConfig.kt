package net.perfectdreams.loritta.morenitta.profile

import kotlinx.serialization.Serializable

@Serializable
data class UserBadgesConfig(
    val badgesConfigs: Map<String, UserBadgeConfig>
) {
    @Serializable
    data class UserBadgeConfig(
        val hidden: Boolean = false,
        val priority: Int?
    )
}