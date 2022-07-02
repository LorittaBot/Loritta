package net.perfectdreams.loritta.cinnamon.pudding.data.notifications

import kotlinx.serialization.Serializable

/**
 * Base class of notifications that are sent/received via PostgreSQL's `LISTEN`/`NOTIFY` system
 */
@Serializable
sealed class LorittaNotification