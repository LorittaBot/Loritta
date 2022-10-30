package net.perfectdreams.loritta.deviouscache.server.processors.guilds

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.GetGuildMemberRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.GetGuildMemberResponse
import net.perfectdreams.loritta.deviouscache.responses.NotFoundResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.GuildKey
import net.perfectdreams.loritta.deviouscache.server.utils.UserKey

class GetGuildMemberProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: GetGuildMemberRequest): DeviousResponse {
        logger.info { "Getting guild member ${request.userId} of guild ${request.guildId}" }

        m.withLock(GuildKey(request.guildId), UserKey(request.userId)) {
            val cachedMembers = m.members[request.guildId] ?: return NotFoundResponse
            val cachedMember = cachedMembers[request.userId] ?: return NotFoundResponse

            return GetGuildMemberResponse(cachedMember)
        }
    }
}