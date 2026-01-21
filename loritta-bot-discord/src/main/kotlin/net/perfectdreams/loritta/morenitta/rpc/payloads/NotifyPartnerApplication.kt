package net.perfectdreams.loritta.morenitta.rpc.payloads

import kotlinx.serialization.Serializable

@Serializable
data class NotifyPartnerApplicationRequest(val applicationId: Long, val guildId: Long, val channelId: Long)

@Serializable
sealed class NotifyPartnerApplicationResponse {
    @Serializable
    data object Success : NotifyPartnerApplicationResponse()

    @Serializable
    data object GuildNotFound : NotifyPartnerApplicationResponse()

    @Serializable
    data object ChannelNotFound : NotifyPartnerApplicationResponse()

    @Serializable
    data object UserNotFound : NotifyPartnerApplicationResponse()
}
