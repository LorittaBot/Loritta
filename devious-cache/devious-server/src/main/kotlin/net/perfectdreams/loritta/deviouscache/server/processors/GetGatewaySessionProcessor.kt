package net.perfectdreams.loritta.deviouscache.server.processors

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.GetGatewaySessionRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.GetGatewaySessionResponse
import net.perfectdreams.loritta.deviouscache.responses.NotFoundResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache

class GetGatewaySessionProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: GetGatewaySessionRequest): DeviousResponse {
        logger.info { "Getting gateway session of shard ${request.shardId}" }

        val gatewaySession = m.gatewaySessions[request.shardId] ?: return NotFoundResponse

        return GetGatewaySessionResponse(
            gatewaySession.sessionId,
            gatewaySession.resumeGatewayUrl,
            gatewaySession.sequence
        )
    }
}