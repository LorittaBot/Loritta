package net.perfectdreams.loritta.serializable

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class UserBannedState(
    val valid: Boolean,
    val bannedAt: Instant,
    val expiresAt: Instant?,
    val reason: String,
    val bannedBy: UserId?,
    val staffNotes: String?,
)