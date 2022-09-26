package net.perfectdreams.loritta.legacy.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class FortniteApiConfig(
        val token: String,
        val creatorCode: String
)