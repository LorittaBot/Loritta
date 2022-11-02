package net.perfectdreams.loritta.deviouscache.server.processors.guilds

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.data.LightweightSnowflake
import net.perfectdreams.loritta.deviouscache.requests.PutGuildsBulkRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.PutGuildsBulkResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.GuildKey

class PutGuildsBulkProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: PutGuildsBulkRequest): DeviousResponse {
        val guildIds = request.requests.map { GuildKey(it.id) }
        m.withLock(*guildIds.toTypedArray()) {
            m.awaitForEntityPersistenceModificationMutex()

            logger.info { "Received Put Guilds Bulk request for ${request.requests.size} guilds!" }
            val newGuilds = mutableSetOf<LightweightSnowflake>()
            for (request in request.requests) {
                if (PutGuildProcessor.processGuild(logger, false, m, request))
                    newGuilds.add(request.id)
            }

            return PutGuildsBulkResponse(newGuilds)
        }
    }
}