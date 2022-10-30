package net.perfectdreams.loritta.deviouscache.server.processors.guilds

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.data.DeviousVoiceStateData
import net.perfectdreams.loritta.deviouscache.requests.PutVoiceStateRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.NotFoundResponse
import net.perfectdreams.loritta.deviouscache.responses.OkResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.GuildKey
import net.perfectdreams.loritta.deviouscache.server.utils.UserKey
import net.perfectdreams.loritta.deviouscache.server.utils.extensions.runIfDifferentAndNotNull

class PutVoiceStateProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: PutVoiceStateRequest): DeviousResponse {
        m.withLock(GuildKey(request.guildId), UserKey(request.userId)) {
            m.awaitForEntityPersistenceModificationMutex()

            logger.info { "Updating voice state of user ${request.userId} on channel ${request.channelId} on guild ${request.guildId}" }

            val channelId = request.channelId

            val currentVoiceStates = m.voiceStates[request.guildId]
            m.voiceStates[request.guildId] = (currentVoiceStates ?: emptyMap())
                .toMutableMap()
                .also {
                    if (channelId != null) {
                        it[request.userId] = DeviousVoiceStateData(
                            request.userId,
                            channelId
                        )
                    } else {
                        it.remove(request.userId)
                    }
                }
            m.dirtyVoiceStates.add(request.guildId)

            return OkResponse
        }
    }
}