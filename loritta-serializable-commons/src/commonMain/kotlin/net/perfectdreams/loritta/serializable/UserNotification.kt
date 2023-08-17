package net.perfectdreams.loritta.serializable

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
sealed class UserNotification {
    abstract val id: Long
    abstract val timestamp: Instant
    abstract val user: UserId
}

@Serializable
data class DailyTaxWarnUserNotification(
    override val id: Long,
    override val timestamp: Instant,
    override val user: UserId,
    val inactivityTaxTimeWillBeTriggeredAt: Instant,
    val currentSonhos: Long,
    val howMuchWillBeRemoved: Long,
    val maxDayThreshold: Int,
    val minimumSonhosForTrigger: Long,
    val tax: Double
) : UserNotification()

@Serializable
data class DailyTaxTaxedUserNotification(
    override val id: Long,
    override val timestamp: Instant,
    override val user: UserId,
    val nextInactivityTaxTimeWillBeTriggeredAt: Instant,
    val currentSonhos: Long,
    val howMuchWasRemoved: Long,
    val maxDayThreshold: Int,
    val minimumSonhosForTrigger: Long,
    val tax: Double
) : UserNotification()

@Serializable
data class CorreiosPackageUpdateUserNotification(
    override val id: Long,
    override val timestamp: Instant,
    override val user: UserId,
    val trackingId: String,
    val event: JsonObject // JSON object containing the event
) : UserNotification()

@Serializable
data class UnknownUserNotification(
    override val id: Long,
    override val timestamp: Instant,
    override val user: UserId
) : UserNotification()