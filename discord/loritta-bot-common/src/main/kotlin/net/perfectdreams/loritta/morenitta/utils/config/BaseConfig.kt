package net.perfectdreams.loritta.morenitta.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class BaseConfig(
    val loritta: LorittaConfig
)