package net.perfectdreams.loritta.deviouscache.server.processors

import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.UnlockConcurrentLoginRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.OkResponse
import net.perfectdreams.loritta.deviouscache.responses.UnlockConflictConcurrentLoginResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache

class UnlockConcurrentLoginProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: UnlockConcurrentLoginRequest): DeviousResponse {
        m.loginBucketMutex.withLock {
            val key = m.loginBucketsLoggingInStatus[request.bucket]
            if (key != request.key) {
                logger.info { "Tried unlocking login bucket ${request.bucket}, but the key doesn't match!" }
                return UnlockConflictConcurrentLoginResponse
            }

            logger.info { "Unlocked login bucket ${request.bucket}, yay!" }

            m.loginBucketsLoggingInStatus.remove(request.bucket)
            return OkResponse
        }
    }
}