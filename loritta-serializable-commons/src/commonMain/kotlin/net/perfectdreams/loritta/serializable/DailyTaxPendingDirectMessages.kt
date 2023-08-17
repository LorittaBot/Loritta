package net.perfectdreams.loritta.serializable

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
sealed class DailyTaxPendingDirectMessage

@Serializable
class UserDailyTaxWarnDirectMessage(
    val inactivityTaxTimeWillBeTriggeredAt: Instant,
    val triggeredWarnAt: Instant,
    val currentSonhos: Long,
    val howMuchWillBeRemoved: Long,
    val maxDayThreshold: Int,
    val minimumSonhosForTrigger: Long,
    val tax: Double
) : DailyTaxPendingDirectMessage()

@Serializable
class UserDailyTaxTaxedDirectMessage(
    val inactivityTaxTimeTriggeredAt: Instant,
    val nextInactivityTaxTimeWillBeTriggeredAt: Instant,
    val currentSonhos: Long,
    val howMuchWasRemoved: Long,
    val maxDayThreshold: Int,
    val minimumSonhosForTrigger: Long,
    val tax: Double
) : DailyTaxPendingDirectMessage()