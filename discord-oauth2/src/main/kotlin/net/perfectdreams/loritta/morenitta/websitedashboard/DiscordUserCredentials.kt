package net.perfectdreams.loritta.morenitta.websitedashboard

import java.time.Instant
import kotlin.time.Duration.Companion.seconds

class DiscordUserCredentials(
    // These are "var" instead of "val" because a session may be updated after it's created!
    var accessToken: String,
    var refreshToken: String,
    var refreshedAt: Instant,
    var expiresIn: Long,
) {
    companion object {
        private val EXPIRATION_MARGIN = 60.seconds
    }

    fun isAccessTokenExpired(): Boolean {
        val now = Instant.now()
        val expiresAt = refreshedAt.plusSeconds(this.expiresIn - EXPIRATION_MARGIN.inWholeSeconds)

        return now >= expiresAt
    }
}