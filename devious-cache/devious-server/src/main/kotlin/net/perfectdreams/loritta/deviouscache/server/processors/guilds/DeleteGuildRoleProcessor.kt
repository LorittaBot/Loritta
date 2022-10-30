package net.perfectdreams.loritta.deviouscache.server.processors.guilds

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.DeleteGuildRoleRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.OkResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.GuildKey

class DeleteGuildRoleProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: DeleteGuildRoleRequest): DeviousResponse {
        m.withLock(GuildKey(request.guildId)) {
            m.awaitForEntityPersistenceModificationMutex()

            logger.info { "Deleting guild role with ID ${request.roleId} on guild ${request.guildId}" }

            val currentRoles = m.roles[request.guildId]
            m.roles[request.guildId] = (currentRoles ?: emptyMap())
                .toMutableMap()
                .also {
                    it.remove(request.roleId)
                }
            m.dirtyRoles.add(request.guildId)
        }

        return OkResponse
    }
}