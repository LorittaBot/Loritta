package net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.respondJson
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse
import net.perfectdreams.sequins.ktor.BaseRoute

class PostLorittaDashboardRpcProcessorRoute(private val m: LorittaDashboardBackend) : BaseRoute("/api/v1/rpc") {
    override suspend fun onRequest(call: ApplicationCall) {
        val body = withContext(Dispatchers.IO) { call.receiveText() }

        val response = when (val request = Json.decodeFromString<LorittaDashboardRPCRequest>(body)) {
            is LorittaDashboardRPCRequest.GetGuildInfoRequest -> {
                m.processors.getGuildInfoProcessor.process(call, request)
            }

            is LorittaDashboardRPCRequest.GetUserGuildsRequest -> {
                m.processors.getUserGuildsProcessor.process(call, request)
            }

            is LorittaDashboardRPCRequest.GetGuildGamerSaferConfigRequest -> {
                m.processors.getGuildGamerSaferConfigProcessor.process(call, request)
            }

            is LorittaDashboardRPCRequest.UpdateGuildGamerSaferConfigRequest -> {
                m.processors.updateGuildGamerSaferConfigProcessor.process(call, request)
            }

            is LorittaDashboardRPCRequest.PutPowerStreamClaimedFirstSonhosRewardRequest -> {
                m.processors.putPowerStreamClaimedFirstSonhosRewardProcessor.process(call, request)
            }

            is LorittaDashboardRPCRequest.PutPowerStreamClaimedLimitedTimeSonhosRewardRequest -> {
                m.processors.putPowerStreamClaimedLimitedTimeSonhosRewardProcessor.process(call, request)
            }

            is LorittaDashboardRPCRequest.UpdateLorittaActivityRequest -> {
                m.processors.updateLorittaActivityProcessor.process(call, request)
            }
        }

        call.respondJson(
            Json.encodeToString<LorittaDashboardRPCResponse>(response)
        )
    }
}