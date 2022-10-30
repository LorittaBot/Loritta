package net.perfectdreams.loritta.deviouscache.server.processors.guilds

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.GetVoiceStateRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.GetVoiceStateResponse
import net.perfectdreams.loritta.deviouscache.responses.NotFoundResponse
import net.perfectdreams.loritta.deviouscache.responses.OkResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.GuildKey
import net.perfectdreams.loritta.deviouscache.server.utils.UserKey

class GetVoiceStateProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: GetVoiceStateRequest): DeviousResponse {
        m.withLock(GuildKey(request.guildId), UserKey(request.userId)) {
            logger.info { "Getting voice state with ID ${request.userId} and guild ID ${request.guildId}" }

            val currentVoiceStates = m.voiceStates[request.guildId] ?: return NotFoundResponse
            val voiceState = currentVoiceStates[request.userId] ?: return NotFoundResponse

            return GetVoiceStateResponse(
                voiceState.channelId
            )
        }
    }
}