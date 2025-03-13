package net.perfectdreams.loritta.serializable

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: UserId,
    val profileSettingsId: Long,
    val money: Long,
    val isAfk: Boolean,
    val afkReason: String?,
    val vacationUntil: Instant?
)