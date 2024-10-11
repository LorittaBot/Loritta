package net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.loritta

import io.ktor.server.application.*
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.LorittaInternalRpcProcessor
import net.perfectdreams.loritta.serializable.LorittaCluster
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse

class GetLorittaInfoProcessor(val m: LorittaBot) : LorittaInternalRpcProcessor<LorittaInternalRPCRequest.GetLorittaInfoRequest, LorittaInternalRPCResponse.GetLorittaInfoResponse> {
    override suspend fun process(
        call: ApplicationCall,
        request: LorittaInternalRPCRequest.GetLorittaInfoRequest
    ): LorittaInternalRPCResponse.GetLorittaInfoResponse {
        return LorittaInternalRPCResponse.GetLorittaInfoResponse.Success(
            m.config.loritta.discord.applicationId.toLong(),
            m.config.loritta.discord.clientSecret,
            m.config.loritta.environment,
            m.config.loritta.discord.maxShards,
            m.config.loritta.clusters.instances.map {
                LorittaCluster(
                    it.id,
                    it.name,
                    it.minShard,
                    it.maxShard,
                    it.websiteUrl,
                    it.rpcUrl
                )
            }
        )
    }
}