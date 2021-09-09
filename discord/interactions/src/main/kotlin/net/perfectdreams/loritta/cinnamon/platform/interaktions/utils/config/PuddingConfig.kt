package net.perfectdreams.loritta.cinnamon.platform.interaktions.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class PuddingConfig(
    val url: String,
    val authorization: String
)