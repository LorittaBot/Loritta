package net.perfectdreams.loritta.deviouscache.server.processors.channels

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.DeleteChannelRequest
import net.perfectdreams.loritta.deviouscache.requests.PutChannelRequest
import net.perfectdreams.loritta.deviouscache.requests.PutUserRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.OkResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.ChannelKey
import net.perfectdreams.loritta.deviouscache.server.utils.UserKey

class DeleteChannelProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: DeleteChannelRequest): DeviousResponse {
        m.withLock(ChannelKey(request.channelId)) {
            logger.info { "Deleting channel with ID ${request.channelId}" }
            val currentChannelData = m.channels[request.channelId]

            m.channels.remove(request.channelId)
            m.dirtyChannels.add(request.channelId)

            val guildId = currentChannelData?.guildId
            if (guildId != null) {
                val cachedGuild = m.guilds[guildId]
                if (cachedGuild != null) {
                    // Remove the channel ID from the cached guild
                    m.guilds[guildId] = cachedGuild.copy(channelIds = (cachedGuild.channelIds - request.channelId))
                    m.dirtyGuilds.add(guildId)
                } else {
                    logger.warn { "Channel ${request.channelId} requires guild $guildId, but we don't have it cached!" }
                }
            }

            return OkResponse
        }
    }
}