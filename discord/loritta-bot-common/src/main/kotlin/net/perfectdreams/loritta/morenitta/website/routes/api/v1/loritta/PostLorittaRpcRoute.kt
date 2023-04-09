package net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.common.utils.daily.DailyRewardQuestions
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.website.utils.extensions.trueIp
import net.perfectdreams.loritta.serializable.requests.*
import net.perfectdreams.loritta.serializable.responses.GetDailyRewardStatusResponse
import net.perfectdreams.loritta.serializable.responses.LorittaRPCResponse
import net.perfectdreams.sequins.ktor.BaseRoute

class PostLorittaRpcRoute(val m: LorittaWebsite) : BaseRoute("/api/v1/loritta/rpc") {
    override suspend fun onRequest(call: ApplicationCall) {
        val body = withContext(Dispatchers.IO) { call.receiveText() }

        val request = Json.decodeFromString<LorittaRPCRequest>(body)

        val response = when (request) {
            is GetDailyRewardStatusRequest -> {
                m.processors.getDailyRewardStatusProcessor.process(call, request)
            }
            is GetDailyRewardRequest -> {
                m.processors.getDailyRewardProcessor.process(call, request)
            }
            is GetGamerSaferVerifyConfigRequest -> {
                m.processors.getGamerSaferVerifyConfigProcessor.process(call, request)
            }
            is PostGamerSaferVerifyConfigRequest -> {
                m.processors.postGamerSaferVerifyConfigProcessor.process(call, request)
            }
        }

        call.respondJson(
            Json.encodeToString<LorittaRPCResponse>(response)
        )
    }
}