package net.perfectdreams.loritta.morenitta.rpc.payloads

import kotlinx.serialization.Serializable

@Serializable
data class QueryGuildInfoRequest(val guildId: Long)

@Serializable
sealed class QueryGuildInfoResponse {
    @Serializable
    data class Success(
        val guildId: Long,
        val name: String,
        val memberCount: Int,
        val iconUrl: String?,
        val ownerId: Long
    ) : QueryGuildInfoResponse()

    @Serializable
    data object GuildNotFound : QueryGuildInfoResponse()
}
