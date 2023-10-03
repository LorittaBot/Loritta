package net.perfectdreams.loritta.serializable.config

import kotlinx.serialization.Serializable

@Serializable
data class TrackedTwitchAccount(
    val id: Long,
    val twitchUserId: Long,
    val channelId: Long,
    val message: String
)