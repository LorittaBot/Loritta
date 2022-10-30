package net.perfectdreams.loritta.deviouscache.server.processors.users

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.GetUserRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.GetUserResponse
import net.perfectdreams.loritta.deviouscache.responses.NotFoundResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.UserKey

class GetUserProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: GetUserRequest): DeviousResponse {
        m.withLock(UserKey(request.id)) {
            logger.info { "Getting user with ID ${request.id}" }
            val deviousUserData = m.users[request.id] ?: return NotFoundResponse

            return GetUserResponse(deviousUserData)
        }
    }
}