package net.perfectdreams.loritta.deviouscache.server.processors.misc

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.GetMiscellaneousDataRequest
import net.perfectdreams.loritta.deviouscache.requests.InvokeManualGCRequest
import net.perfectdreams.loritta.deviouscache.requests.PutMiscellaneousDataRequest
import net.perfectdreams.loritta.deviouscache.requests.PutUserRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.GetMiscellaneousDataResponse
import net.perfectdreams.loritta.deviouscache.responses.NotFoundResponse
import net.perfectdreams.loritta.deviouscache.responses.OkResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.UserKey

class InvokeManualGCProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: InvokeManualGCRequest): DeviousResponse {
        System.gc()
        return OkResponse
    }
}