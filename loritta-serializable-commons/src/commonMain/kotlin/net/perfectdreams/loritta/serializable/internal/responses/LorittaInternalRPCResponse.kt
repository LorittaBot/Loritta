package net.perfectdreams.loritta.serializable.internal.responses

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.EnvironmentType
import net.perfectdreams.loritta.serializable.LorittaCluster

@Serializable
sealed class LorittaInternalRPCResponse {
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
}