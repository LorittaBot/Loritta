package net.perfectdreams.loritta.deviouscache.server.processors.misc

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.PutMiscellaneousDataRequest
import net.perfectdreams.loritta.deviouscache.requests.PutUserRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.OkResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.UserKey

class PutMiscellaneousDataProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: PutMiscellaneousDataRequest): DeviousResponse {
        val miscMutex = m.miscellaneousDataMutex.getOrPut(request.key) { Mutex() }
        miscMutex.withLock {
            m.miscellaneousData[request.key] = request.data

            return OkResponse
        }
    }
}