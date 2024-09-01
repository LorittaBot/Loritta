package net.perfectdreams.loritta.serializable.internal.responses

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.EnvironmentType
import net.perfectdreams.loritta.serializable.LorittaCluster
import net.perfectdreams.loritta.serializable.dashboard.responses.DashGuildScopedResponse

@Serializable
sealed class LorittaInternalRPCResponse {
    @Serializable
    class ExecuteDashGuildScopedRPCResponse(val response: DashGuildScopedResponse) : LorittaInternalRPCResponse()

    @Serializable
    sealed class GetLorittaInfoResponse : LorittaInternalRPCResponse() {
        @Serializable
        class Success(
            val clientId: Long,
            val clientSecret: String,
            val environmentType: EnvironmentType,
            val maxShards: Int,
            val instances: List<LorittaCluster>
        ) : GetLorittaInfoResponse()
    }

    @Serializable
    data object UpdateTwitchSubscriptionsResponse : LorittaInternalRPCResponse()

    @Serializable
    data class TwitchStreamOnlineEventResponse(val notifiedGuilds: List<Long>) : LorittaInternalRPCResponse()

    @Serializable
    data class BlueskyPostRelayResponse(val notifiedGuilds: List<Long>) : LorittaInternalRPCResponse()
}