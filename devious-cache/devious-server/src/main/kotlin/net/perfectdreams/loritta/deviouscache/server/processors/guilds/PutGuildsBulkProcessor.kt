package net.perfectdreams.loritta.deviouscache.server.processors.guilds

import dev.kord.common.entity.Snowflake
import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.PutGuildRequest
import net.perfectdreams.loritta.deviouscache.requests.PutGuildsBulkRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.OkResponse
import net.perfectdreams.loritta.deviouscache.responses.PutGuildsBulkResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.DeviousGuildDataWrapper
import net.perfectdreams.loritta.deviouscache.server.utils.GuildAndUserPair
import net.perfectdreams.loritta.deviouscache.server.utils.GuildKey
import net.perfectdreams.loritta.deviouscache.server.utils.extensions.runIfDifferentAndNotNull

class PutGuildsBulkProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: PutGuildsBulkRequest): DeviousResponse {
        val guildIds = request.requests.map { GuildKey(it.id) }
        m.withLock(*guildIds.toTypedArray()) {
            m.awaitForEntityPersistenceModificationMutex()

            logger.info { "Received Put Guilds Bulk request for ${request.requests} guilds!" }
            val newGuilds = mutableSetOf<Snowflake>()
            for (request in request.requests) {
                if (PutGuildProcessor.processGuild(logger, m, request))
                    newGuilds.add(request.id)
            }

            return PutGuildsBulkResponse(newGuilds)
        }
    }
}