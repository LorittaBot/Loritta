package net.perfectdreams.loritta.deviouscache.server.processors.users

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.PutUserRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.OkResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.UserKey

class PutUserProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: PutUserRequest): DeviousResponse {
        m.withLock(UserKey(request.id)) {
            val deviousUserData = request.data
            val currentUserData = m.users[request.id]

            if (deviousUserData != currentUserData) {
                m.awaitForEntityPersistenceModificationMutex()

                logger.info { "Updating user with ID ${request.id}" }
                m.users[request.id] = deviousUserData
                m.dirtyUsers.add(request.id)
            } else {
                logger.info { "Noop operation on ${request.id}"}
            }

            return OkResponse
        }
    }
}