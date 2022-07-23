package net.perfectdreams.loritta.cinnamon.pudding.data.notifications

import kotlinx.serialization.Serializable

@Serializable
sealed class LorittaVoiceConnectionStateNotification

@Serializable
data class LorittaVoiceConnectionStateRequest(
    override val uniqueId: String,
    val guildId: Long
) : FalatronNotification(), LorittaNotificationResponse

@Serializable
data class LorittaVoiceConnectionStateResponse(
    override val uniqueId: String,
    val channelId: Long?,
    val playing: Boolean,
) : FalatronNotification(), LorittaNotificationResponse