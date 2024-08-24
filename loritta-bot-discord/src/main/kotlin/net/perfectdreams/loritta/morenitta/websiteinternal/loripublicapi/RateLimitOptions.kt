package net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi

import kotlin.time.Duration

data class RateLimitOptions(
    val totalAllowedRequests: Int,
    val rateLimitResetAfter: Duration
)