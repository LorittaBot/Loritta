package net.perfectdreams.loritta.deviouscache.server.processors.channels

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.GetChannelRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.GetChannelResponse
import net.perfectdreams.loritta.deviouscache.responses.GetGuildChannelResponse
import net.perfectdreams.loritta.deviouscache.responses.NotFoundResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.ChannelKey

class GetChannelProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: GetChannelRequest): DeviousResponse {
        m.withLock(ChannelKey(request.channelId)) {
            logger.info { "Getting channel ${request.channelId}" }

            val cachedChannel = m.channels[request.channelId] ?: return NotFoundResponse
            val guildId = cachedChannel.guildId
            val cachedGuild = if (guildId != null)
                m.guilds[guildId]
            else
                null

            return if (cachedGuild != null && guildId != null) {
                val cachedGuildData = cachedGuild.data
                val roles = m.roles[guildId]
                val channels = cachedGuild.channelIds.mapNotNull { m.channels[it] }.associateBy { it.id }
                val emotes = m.emotes[guildId]

                GetGuildChannelResponse(
                    cachedChannel,
                    cachedGuildData,
                    roles?.toMap() ?: emptyMap(),
                    channels,
                    emotes?.toMap() ?: emptyMap()
                )
            } else {
                GetChannelResponse(cachedChannel)
            }
        }
    }
}