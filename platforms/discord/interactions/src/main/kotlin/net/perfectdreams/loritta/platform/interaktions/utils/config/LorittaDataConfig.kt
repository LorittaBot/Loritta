package net.perfectdreams.loritta.platform.interaktions.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class LorittaDataConfig(
    val type: String,
    val pudding: PuddingConfig?
)