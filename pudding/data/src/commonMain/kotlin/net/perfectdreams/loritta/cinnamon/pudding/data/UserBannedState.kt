package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class UserBannedState(
    val userId: Long,
    val valid: Boolean,
    val bannedAt: Instant,
    val expiresAt: Instant?,
    val reason: String,
    val bannedBy: UserId?
)