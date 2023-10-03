package net.perfectdreams.switchtwitch.data

import kotlinx.serialization.Serializable

@Serializable
data class GetUsersResponse(
    val data: List<TwitchUser>
)