package net.perfectdreams.loritta.deviouscache.server.processors.guilds

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.PutGuildEmojisRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.OkResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.GuildKey

class PutGuildEmojisProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: PutGuildEmojisRequest): DeviousResponse {
        m.withLock(GuildKey(request.guildId)) {
            m.awaitForEntityPersistenceModificationMutex()

            logger.info { "Updating guild emojis on guild ${request.guildId}" }

            m.emotes[request.guildId] = request.emojis.associateBy { it.id }
            m.dirtyEmojis.add(request.guildId)

            return OkResponse
        }
    }
}