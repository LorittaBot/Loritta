package net.perfectdreams.loritta.deviouscache.server.processors.guilds

import dev.kord.common.entity.Snowflake
import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.GetGuildWithEntitiesRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.GetGuildWithEntitiesResponse
import net.perfectdreams.loritta.deviouscache.responses.NotFoundResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.GuildKey

class GetGuildWithEntitiesProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: GetGuildWithEntitiesRequest): DeviousResponse {
        m.withLock(GuildKey(request.id)) {
            logger.info { "Getting guild + entities with ID ${request.id}" }

            val cachedGuild = m.guilds[request.id] ?: return NotFoundResponse
            val cachedGuildData = cachedGuild.data
            val roles = m.roles[request.id]
            val channels = cachedGuild.channelIds.mapNotNull { m.channels[it] }.associateBy { it.id }
            val emotes = m.emotes[request.id]

            return GetGuildWithEntitiesResponse(
                cachedGuildData,
                roles?.toMap() ?: emptyMap(),
                channels,
                emotes?.toMap() ?: emptyMap()
            )
        }
    }
}