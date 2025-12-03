package net.perfectdreams.loritta.morenitta.rpc.payloads

import kotlinx.serialization.Serializable

@Serializable
data class TwitchStreamOnlineEventRequest(
    val twitchUserId: Long,
    val twitchUserLogin: String,
    val title: String?,
    val gameName: String?,
)

@Serializable
sealed class TwitchStreamOnlineEventResponse {
    @Serializable
    data class Success(val notifiedGuilds: List<Long>) : TwitchStreamOnlineEventResponse()
}