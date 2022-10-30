package net.perfectdreams.loritta.deviouscache.server.processors

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.PutGatewaySequenceRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.OkResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache

class PutGatewaySequenceProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: PutGatewaySequenceRequest): DeviousResponse {
        logger.info { "Putting gateway sequence of shard ${request.shardId}" }

        m.gatewaySessions[request.shardId]?.sequence = request.sequence

        return OkResponse
    }
}