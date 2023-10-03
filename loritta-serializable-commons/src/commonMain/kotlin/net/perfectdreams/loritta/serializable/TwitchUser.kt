package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
data class TwitchUser(
    val id: Long,
    val login: String,
    val displayName: String,
    val profileImageUrl: String
)