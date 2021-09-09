package net.perfectdreams.loritta.cinnamon.platform.interaktions.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class LorittaDataConfig(
    val type: String,
    val pudding: PuddingConfig?
)