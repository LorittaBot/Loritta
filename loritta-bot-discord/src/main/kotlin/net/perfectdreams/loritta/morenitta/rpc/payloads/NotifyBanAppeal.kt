package net.perfectdreams.loritta.morenitta.rpc.payloads

import kotlinx.serialization.Serializable

@Serializable
data class NotifyBanAppealRequest(val appealId: Long, val guildId: Long, val channelId: Long)

@Serializable
sealed class NotifyBanAppealResponse {
    @Serializable
    data object Success : NotifyBanAppealResponse()

    @Serializable
    data object UserNotFound : NotifyBanAppealResponse()
}