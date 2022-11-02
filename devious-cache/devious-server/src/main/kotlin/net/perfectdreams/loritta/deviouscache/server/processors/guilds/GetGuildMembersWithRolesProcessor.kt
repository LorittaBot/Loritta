package net.perfectdreams.loritta.deviouscache.server.processors.guilds

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.data.DeviousUserAndMember
import net.perfectdreams.loritta.deviouscache.data.LightweightSnowflake
import net.perfectdreams.loritta.deviouscache.requests.GetGuildMembersWithRolesRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.GetGuildMembersResponse
import net.perfectdreams.loritta.deviouscache.responses.NotFoundResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.GuildKey

class GetGuildMembersWithRolesProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: GetGuildMembersWithRolesRequest): DeviousResponse {
        m.withLock(GuildKey(request.guildId)) {
            logger.info { "Getting guild members with roles ${request.roles} of guild ${request.guildId}" }

            val cachedMembers = m.members[request.guildId] ?: return NotFoundResponse

            val map = mutableMapOf<LightweightSnowflake, DeviousUserAndMember>()
            for ((id, member) in cachedMembers) {
                if (!member.roles.containsAll(request.roles))
                    continue

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