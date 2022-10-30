package net.perfectdreams.loritta.deviouscache.server.processors.guilds

import dev.kord.common.entity.Snowflake
import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.data.DeviousUserAndMember
import net.perfectdreams.loritta.deviouscache.requests.GetGuildBoostersRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.GetGuildMembersResponse
import net.perfectdreams.loritta.deviouscache.responses.NotFoundResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.GuildKey

class GetGuildBoostersProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: GetGuildBoostersRequest): DeviousResponse {
        m.withLock(GuildKey(request.guildId)) {
            logger.info { "Getting guild boosters of guild ${request.guildId}" }

            val cachedMembers = m.members[request.guildId] ?: return NotFoundResponse

            val map = mutableMapOf<Snowflake, DeviousUserAndMember>()
            for ((id, member) in cachedMembers) {
                if (member.premiumSince == null)
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