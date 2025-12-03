package net.perfectdreams.loritta.morenitta.rpc

import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.rpc.payloads.BlueskyPostRelayRequest
import net.perfectdreams.loritta.morenitta.rpc.payloads.BlueskyPostRelayResponse
import net.perfectdreams.loritta.morenitta.rpc.payloads.DailyShopRefreshedRequest
import net.perfectdreams.loritta.morenitta.rpc.payloads.DailyShopRefreshedResponse
import net.perfectdreams.loritta.morenitta.rpc.payloads.NotifyBanAppealRequest
import net.perfectdreams.loritta.morenitta.rpc.payloads.NotifyBanAppealResponse
import net.perfectdreams.loritta.morenitta.rpc.payloads.TwitchStreamOnlineEventRequest
import net.perfectdreams.loritta.morenitta.rpc.payloads.TwitchStreamOnlineEventResponse
import net.perfectdreams.loritta.morenitta.rpc.payloads.UpdateTwitchSubscriptionsResponse
import net.perfectdreams.loritta.morenitta.utils.config.LorittaConfig

object LorittaRPC {
    val NotifyBanAppeal = LorittaRPCCommandName<NotifyBanAppealRequest, NotifyBanAppealResponse>("notifyBanAppeal")
    val UpdateTwitchSubscriptions = LorittaRPCCommandName<Unit, UpdateTwitchSubscriptionsResponse>("updateTwitchSubscriptions")
    val TwitchStreamOnlineEvent = LorittaRPCCommandName<TwitchStreamOnlineEventRequest, TwitchStreamOnlineEventResponse>("twitchStreamOnlineEvent")
    val BlueskyPostRelay = LorittaRPCCommandName<BlueskyPostRelayRequest, BlueskyPostRelayResponse>("blueskyPostRelay")
    val DailyShopRefreshed = LorittaRPCCommandName<DailyShopRefreshedRequest, DailyShopRefreshedResponse>("dailyShopRefreshed")

    class LorittaRPCCommandName<RequestType, ResponseType>(val name: String)
}

suspend inline fun <reified RequestType, reified ResponseType> LorittaRPC.LorittaRPCCommandName<RequestType, ResponseType>.execute(
    loritta: LorittaBot,
    cluster: LorittaConfig.LorittaClustersConfig.LorittaClusterConfig,
    request: RequestType
): ResponseType {
    val response = loritta.http.post(cluster.rpcUrl.removePrefix("/") + "/lorirpc/${this.name}") {
        if (request !is Unit) {
            setBody(Json.encodeToString(request))
        }
    }

    return Json.decodeFromString(response.body())
}