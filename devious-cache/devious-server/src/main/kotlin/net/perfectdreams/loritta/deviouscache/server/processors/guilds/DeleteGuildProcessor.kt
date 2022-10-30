package net.perfectdreams.loritta.deviouscache.server.processors.guilds

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.DeleteGuildRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.NotFoundResponse
import net.perfectdreams.loritta.deviouscache.responses.OkResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.GuildAndUserPair
import net.perfectdreams.loritta.deviouscache.server.utils.GuildKey

class DeleteGuildProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: DeleteGuildRequest): DeviousResponse {
        m.withLock(GuildKey(request.id)) {
            m.awaitForEntityPersistenceModificationMutex()

            logger.info { "Deleting guild with ID ${request.id}" }

            val cachedGuild = m.guilds[request.id] ?: return NotFoundResponse
            cachedGuild.channelIds.forEach {
                m.channels.remove(it)
                m.dirtyChannels.add(it)
            }

            m.roles.remove(request.id)
            m.dirtyRoles.add(request.id)

            m.emotes.remove(request.id)
            m.dirtyEmojis.add(request.id)

            val members = m.members[request.id]
            if (members != null) {
                m.members.remove(request.id)
                for (member in members) {
                    // Bust the cache of all the members on this guild
                    m.dirtyMembers.add(GuildAndUserPair(request.id, member.key))
                }
            }

            m.guilds.remove(request.id)
            m.dirtyGuilds.add(request.id)
        }

        return OkResponse
    }
}