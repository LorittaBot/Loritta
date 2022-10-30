package net.perfectdreams.loritta.deviouscache.server.processors

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.data.DeviousGatewaySession
import net.perfectdreams.loritta.deviouscache.requests.PutGatewaySessionRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.OkResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache

class PutGatewaySessionProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: PutGatewaySessionRequest): DeviousResponse {
        logger.info { "Putting gateway session of shard ${request.shardId}" }

        m.gatewaySessions[request.shardId] = DeviousGatewaySession(
            request.sessionId,
            request.resumeGatewayUrl,
            request.sequence
        )

        return OkResponse
    }
}