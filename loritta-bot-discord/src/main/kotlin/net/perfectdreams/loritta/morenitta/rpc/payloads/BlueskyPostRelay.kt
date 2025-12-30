package net.perfectdreams.loritta.morenitta.rpc.payloads

import kotlinx.serialization.Serializable

@Serializable
data class BlueskyPostRelayRequest(
    val repo: String,
    val postId: String,
    val tracks: List<TrackInfo>
) {
    @Serializable
    data class TrackInfo(
        val guildId: Long,
        val channelId: Long,
        val message: String
    )
}

@Serializable
sealed class BlueskyPostRelayResponse {
    @Serializable
    data class Success(val notifiedGuilds: List<Long>) : BlueskyPostRelayResponse()
}