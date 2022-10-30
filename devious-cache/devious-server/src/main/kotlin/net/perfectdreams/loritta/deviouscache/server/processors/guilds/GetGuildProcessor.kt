package net.perfectdreams.loritta.deviouscache.server.processors.guilds

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.GetGuildRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.GetGuildResponse
import net.perfectdreams.loritta.deviouscache.responses.NotFoundResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.GuildKey

class GetGuildProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: GetGuildRequest): DeviousResponse {
        m.withLock(GuildKey(request.id)) {
            logger.info { "Getting guild with ID ${request.id}" }

            val cachedGuild = m.guilds[request.id]
            val cachedGuildData = cachedGuild?.data ?: return NotFoundResponse

            return GetGuildResponse(cachedGuildData)
        }
    }
}