package net.perfectdreams.loritta.deviouscache.server.processors.guilds

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.GetGuildCountRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.GetGuildCountResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache

class GetGuildCountProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: GetGuildCountRequest): DeviousResponse {
        logger.info { "Getting guild count" }

        return GetGuildCountResponse(m.guilds.size.toLong())
    }
}