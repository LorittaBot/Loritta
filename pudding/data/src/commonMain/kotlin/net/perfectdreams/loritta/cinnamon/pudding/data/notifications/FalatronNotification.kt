package net.perfectdreams.loritta.cinnamon.pudding.data.notifications

import kotlinx.serialization.Serializable

@Serializable
sealed class FalatronNotification : LorittaNotification()

@Serializable
data class FalatronVoiceRequest(
    override val uniqueId: String,
    val guildId: Long,
    val channelId: Long,
    val voice: String,
    val text: String
) : FalatronNotification(), LorittaNotificationRequest

@Serializable
data class FalatronVoiceRequestReceivedResponseX(
    override val uniqueId: String
) : FalatronNotification(), LorittaNotificationResponse

@Serializable
data class FalatronOfflineErrorResponse(
    override val uniqueId: String
) : FalatronNotification(), LorittaNotificationResponse

@Serializable
data class FailedToConnectToVoiceChannelResponse(
    override val uniqueId: String
) : FalatronNotification(), LorittaNotificationResponse

@Serializable
data class FalatronVoiceResponse(
    override val uniqueId: String,
    val queued: Boolean
) : FalatronNotification(), LorittaNotificationResponse