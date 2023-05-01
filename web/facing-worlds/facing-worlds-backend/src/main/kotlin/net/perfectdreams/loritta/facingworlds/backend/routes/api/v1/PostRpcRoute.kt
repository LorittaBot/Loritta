package net.perfectdreams.loritta.facingworlds.backend.routes.api.v1

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.facingworlds.backend.FacingWorldsBackend
import net.perfectdreams.loritta.facingworlds.backend.utils.respondJson
import net.perfectdreams.loritta.facingworlds.common.v1.FacingWorldsRPCRequest
import net.perfectdreams.loritta.facingworlds.common.v1.FacingWorldsRPCResponse
import net.perfectdreams.loritta.facingworlds.common.v1.PutPowerStreamClaimedFirstSonhosRewardRequest
import net.perfectdreams.loritta.facingworlds.common.v1.PutPowerStreamClaimedLimitedTimeSonhosRewardRequest
import net.perfectdreams.sequins.ktor.BaseRoute

class PostRpcRoute(private val m: FacingWorldsBackend) : BaseRoute("/v1/rpc") {
    override suspend fun onRequest(call: ApplicationCall) {
        val body = withContext(Dispatchers.IO) { call.receiveText() }

        val request = Json.decodeFromString<FacingWorldsRPCRequest>(body)

        val response = when (request) {
            is PutPowerStreamClaimedLimitedTimeSonhosRewardRequest -> {
                m.processors.putPowerStreamClaimedLimitedTimeSonhosRewardProcessor.process(call, request)
            }

            is PutPowerStreamClaimedFirstSonhosRewardRequest -> {
                m.processors.putPowerStreamClaimedFirstSonhosRewardProcessor.process(call, request)
            }
        }

        call.respondJson(
            Json.encodeToString<FacingWorldsRPCResponse>(response.response),
            response.status
        )
    }
}