package net.perfectdreams.loritta.deviouscache.server.processors.channels

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.PutChannelRequest
import net.perfectdreams.loritta.deviouscache.requests.PutUserRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.OkResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.ChannelKey
import net.perfectdreams.loritta.deviouscache.server.utils.UserKey

class PutChannelProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: PutChannelRequest): DeviousResponse {
        m.withLock(ChannelKey(request.channelId)) {
            val deviousChannelData = request.data
            val currentChannelData = m.channels[request.channelId]

            if (deviousChannelData != currentChannelData) {
                m.awaitForEntityPersistenceModificationMutex()

                logger.info { "Updating channel with ID ${request.channelId}" }
                m.channels[request.channelId] = deviousChannelData
                m.dirtyChannels.add(request.channelId)

                val guildId = request.data.guildId
                if (guildId != null) {
                    val cachedGuild = m.guilds[guildId]
                    if (cachedGuild != null) {
                        // Add the channel ID to the cached guild
                        m.guilds[guildId] = cachedGuild.copy(channelIds = (cachedGuild.channelIds + deviousChannelData.id))
                        m.dirtyGuilds.add(guildId)
                    } else {
                        logger.warn { "Channel ${request.channelId} requires guild $guildId, but we don't have it cached!" }
                    }
                }
            } else {
                logger.info { "Noop operation on ${request.channelId}"}
            }

            return OkResponse
        }
    }
}