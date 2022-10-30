package net.perfectdreams.loritta.deviouscache.server.processors.guilds

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.GetIfGuildExistsRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.NotFoundResponse
import net.perfectdreams.loritta.deviouscache.responses.OkResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.GuildKey

class GetIfGuildExistsProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: GetIfGuildExistsRequest): DeviousResponse {
        m.withLock(GuildKey(request.id)) {
            logger.info { "Getting if guild with ID ${request.id} exists" }

            val exists = m.guilds.containsKey(request.id)
            return if (exists)
                OkResponse
            else
                NotFoundResponse
        }
    }
}