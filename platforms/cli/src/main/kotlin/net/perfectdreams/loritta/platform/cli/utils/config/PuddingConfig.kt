package net.perfectdreams.loritta.platform.cli.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class PuddingConfig(
    val url: String,
    val authorization: String
)