package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
sealed class MessageQueuePayload

@Serializable
class DirectMessageUserDailyTaxWarnMessageQueuePayload(
    val userId: UserId,
    val time: Instant,
    val currentSonhos: Long,
    val howMuchWillBeRemoved: Long,
    val maxDayThreshold: Long,
    val minimumSonhosForTrigger: Long,
    val tax: Double
) : MessageQueuePayload()

@Serializable
class DirectMessageUserDailyTaxTaxedMessageQueuePayload(
    val userId: UserId,
    val time: Instant,
    val currentSonhos: Long,
    val howMuchWasRemoved: Long,
    val maxDayThreshold: Long,
    val minimumSonhosForTrigger: Long,
    val tax: Double
) : MessageQueuePayload()

@Serializable
object UnknownMessageQueuePayload : MessageQueuePayload()