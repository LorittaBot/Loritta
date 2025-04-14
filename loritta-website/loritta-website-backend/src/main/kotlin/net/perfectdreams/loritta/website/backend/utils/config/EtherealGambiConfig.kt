package net.perfectdreams.loritta.website.backend.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class EtherealGambiConfig(
    val url: String,
    val apiUrl: String
)