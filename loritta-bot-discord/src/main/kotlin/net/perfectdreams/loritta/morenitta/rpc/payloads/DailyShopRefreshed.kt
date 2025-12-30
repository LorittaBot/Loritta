package net.perfectdreams.loritta.morenitta.rpc.payloads

import kotlinx.serialization.Serializable

@Serializable
data class DailyShopRefreshedRequest(val dailyShopId: Long)

@Serializable
sealed class DailyShopRefreshedResponse {
    @Serializable
    data object Success : DailyShopRefreshedResponse()
}