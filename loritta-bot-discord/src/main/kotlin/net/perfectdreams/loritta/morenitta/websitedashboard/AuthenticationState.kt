package net.perfectdreams.loritta.morenitta.websitedashboard

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationState(
    val source: String? = null,
    val medium: String? = null,
    val campaign: String? = null,
    val content: String? = null,
    val httpReferrer: String? = null,
    val redirectUrl: String? = null
)