package net.perfectdreams.loritta.deviouscache.server.processors

import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.LockConcurrentLoginRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.LockConflictConcurrentLoginResponse
import net.perfectdreams.loritta.deviouscache.responses.LockSuccessfulConcurrentLoginResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import kotlin.random.Random

class LockConcurrentLoginProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val random = Random.Default

    suspend fun process(request: LockConcurrentLoginRequest): DeviousResponse {
        m.loginBucketMutex.withLock {
            if (m.loginBucketsLoggingInStatus.contains(request.bucket)) {
                logger.info { "Requested bucket ${request.bucket} to be locked for login, but it is already being in use!" }

                return LockConflictConcurrentLoginResponse
            } else {
                val randomKey = random.nextBytes(20).toString(Charsets.UTF_8)
                m.loginBucketsLoggingInStatus[request.bucket] = randomKey

                logger.info { "Locked bucket ${request.bucket} for login! Gotta go fast!" }

                return LockSuccessfulConcurrentLoginResponse(randomKey)
            }
        }
    }
}