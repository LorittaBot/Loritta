package net.perfectdreams.loritta.cinnamon.pudding.data.notifications

import kotlinx.serialization.Serializable

/**
 * Base class of notifications that are sent/received via PostgreSQL's `LISTEN`/`NOTIFY` system
 *
 * All notifications have an "Unique ID" that can be used to track requests/responses
 */
@Serializable
sealed class LorittaNotification {
    abstract val uniqueId: String
}

interface LorittaNotificationRequest
interface LorittaNotificationResponse