package net.perfectdreams.loritta.morenitta.rpc.payloads

import kotlinx.serialization.Serializable

@Serializable
sealed class UpdateTwitchSubscriptionsResponse {
    @Serializable
    data object Success : UpdateTwitchSubscriptionsResponse()
}