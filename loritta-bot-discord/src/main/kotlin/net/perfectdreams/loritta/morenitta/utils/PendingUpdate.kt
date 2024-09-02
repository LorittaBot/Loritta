package net.perfectdreams.loritta.morenitta.utils

import kotlinx.datetime.Instant

/**
 * A class that stores a pending Loritta update
 */
data class PendingUpdate(
    val requestedAt: Instant
)