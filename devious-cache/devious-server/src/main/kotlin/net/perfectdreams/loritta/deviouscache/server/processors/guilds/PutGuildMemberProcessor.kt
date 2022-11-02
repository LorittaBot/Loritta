package net.perfectdreams.loritta.deviouscache.server.processors.guilds

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.data.DeviousMemberData
import net.perfectdreams.loritta.deviouscache.requests.PutGuildMemberRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.PutGuildMemberResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.GuildAndUserPair
import net.perfectdreams.loritta.deviouscache.server.utils.GuildKey
import net.perfectdreams.loritta.deviouscache.server.utils.SnowflakeMap
import net.perfectdreams.loritta.deviouscache.server.utils.UserKey

class PutGuildMemberProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: PutGuildMemberRequest): DeviousResponse {
        m.withLock(GuildKey(request.guildId), UserKey(request.userId)) {
            m.awaitForEntityPersistenceModificationMutex()

            logger.info { "Updating guild member with ID ${request.userId} on guild ${request.guildId}" }

            var oldMember: DeviousMemberData? = null

            val currentMembers = m.members[request.guildId]
            // Expected 1 because we will insert the new member
            m.members[request.guildId] = (currentMembers ?: SnowflakeMap(1))
                .also {
                    oldMember = it[request.userId]
                    it[request.userId] = request.member
                }
            m.dirtyMembers.add(GuildAndUserPair(request.guildId, request.userId))

            return PutGuildMemberResponse(
                oldMember,
                request.member
            )
        }
    }
}