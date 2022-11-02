package net.perfectdreams.loritta.deviouscache.server.processors.guilds

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.data.DeviousUserAndMember
import net.perfectdreams.loritta.deviouscache.data.LightweightSnowflake
import net.perfectdreams.loritta.deviouscache.requests.GetGuildMembersRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.GetGuildMembersResponse
import net.perfectdreams.loritta.deviouscache.responses.NotFoundResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.GuildKey

class GetGuildMembersProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: GetGuildMembersRequest): DeviousResponse {
        logger.info { "Getting guild members of guild ${request.guildId}" }

        m.withLock(GuildKey(request.guildId)) {
            val cachedMembers = m.members[request.guildId] ?: return NotFoundResponse

            val map = mutableMapOf<LightweightSnowflake, DeviousUserAndMember>()
            for ((id, member) in cachedMembers) {
                val user = m.users[id] ?: continue

                map[id] = DeviousUserAndMember(
                    user,
                    member
                )
            }

            return GetGuildMembersResponse(map)
        }
    }
}