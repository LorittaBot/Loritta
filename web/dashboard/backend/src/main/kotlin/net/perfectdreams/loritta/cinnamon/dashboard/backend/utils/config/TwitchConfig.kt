package net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class TwitchConfig(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
)