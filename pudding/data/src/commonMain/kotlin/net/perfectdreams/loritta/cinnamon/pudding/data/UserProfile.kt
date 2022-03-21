package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: UserId,
    val profileSettingsId: Long,
    val money: Long,
    val isAfk: Boolean,
    val afkReason: String?,
    val marriage: Long?
)