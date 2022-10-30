package net.perfectdreams.loritta.deviouscache.server.processors.guilds

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.DeleteGuildMemberRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.OkResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.GuildAndUserPair
import net.perfectdreams.loritta.deviouscache.server.utils.GuildKey
import net.perfectdreams.loritta.deviouscache.server.utils.UserKey

class DeleteGuildMemberProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: DeleteGuildMemberRequest): DeviousResponse {
        m.withLock(GuildKey(request.guildId), UserKey(request.userId)) {
            m.awaitForEntityPersistenceModificationMutex()

            logger.info { "Deleting guild member with ID ${request.userId} on guild ${request.guildId}" }

            val currentMembers = m.members[request.guildId]
            m.members[request.guildId] = (currentMembers ?: emptyMap())
                .toMutableMap()
                .also {
                    it.remove(request.userId)
                }
            m.dirtyMembers.add(GuildAndUserPair(request.guildId, request.userId))

            return OkResponse
        }
    }
}