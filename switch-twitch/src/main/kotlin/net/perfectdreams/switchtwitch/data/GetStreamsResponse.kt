package net.perfectdreams.switchtwitch.data

import kotlinx.serialization.Serializable

@Serializable
data class GetStreamsResponse(
    val data: List<TwitchStream>,
    val pagination: Pagination
)