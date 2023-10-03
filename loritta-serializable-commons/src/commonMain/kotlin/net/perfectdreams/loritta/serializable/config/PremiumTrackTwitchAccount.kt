package net.perfectdreams.loritta.serializable.config

import kotlinx.serialization.Serializable

@Serializable
data class PremiumTrackTwitchAccount(
    val id: Long,
    val twitchUserId: Long
)