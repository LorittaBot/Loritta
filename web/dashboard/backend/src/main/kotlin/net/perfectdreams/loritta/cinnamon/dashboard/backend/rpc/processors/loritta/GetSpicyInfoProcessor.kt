package net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.loritta

import io.ktor.server.application.*
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.LorittaDashboardRpcProcessor
import net.perfectdreams.loritta.serializable.PublicLorittaCluster
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse

class GetSpicyInfoProcessor(val m: LorittaDashboardBackend) : LorittaDashboardRpcProcessor<LorittaDashboardRPCRequest.GetSpicyInfoRequest, LorittaDashboardRPCResponse.GetSpicyInfoResponse> {
    override suspend fun process(
        call: ApplicationCall,
        request: LorittaDashboardRPCRequest.GetSpicyInfoRequest
    ): LorittaDashboardRPCResponse.GetSpicyInfoResponse {
        return LorittaDashboardRPCResponse.GetSpicyInfoResponse.Success(
            "Howdy! Did you know that Loritta is open source? https://github.com/LorittaBot/Loritta :3",
            m.config.legacyDashboardUrl.removeSuffix("/"),
            m.lorittaInfo.clientId,
            m.lorittaInfo.environmentType,
            m.lorittaInfo.maxShards,
            m.lorittaInfo.instances.map {
                PublicLorittaCluster(
                    it.id,
                    it.name,
                    it.minShard,
                    it.maxShard,
                    it.websiteUrl
                )
            },
            m.config.twitch.clientId,
            m.config.twitch.redirectUri
        )
    }
}