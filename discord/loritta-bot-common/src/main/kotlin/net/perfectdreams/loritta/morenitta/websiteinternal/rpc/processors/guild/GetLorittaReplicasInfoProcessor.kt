package net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.guild

import io.ktor.server.application.*
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.LorittaInternalRpcProcessor
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse

class GetLorittaReplicasInfoProcessor(val m: LorittaBot) : LorittaInternalRpcProcessor<LorittaInternalRPCRequest.GetLorittaReplicasInfoRequest, LorittaInternalRPCResponse.GetLorittaReplicasInfoResponse> {
    override suspend fun process(
        call: ApplicationCall,
        request: LorittaInternalRPCRequest.GetLorittaReplicasInfoRequest
    ): LorittaInternalRPCResponse.GetLorittaReplicasInfoResponse {
        return LorittaInternalRPCResponse.GetLorittaReplicasInfoResponse.Success(
            m.config.loritta.discord.maxShards,
            m.config.loritta.clusters.instances.map {
                LorittaInternalRPCResponse.GetLorittaReplicasInfoResponse.LorittaCluster(
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