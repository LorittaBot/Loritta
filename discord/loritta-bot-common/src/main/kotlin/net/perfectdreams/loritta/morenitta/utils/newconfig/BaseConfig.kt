package net.perfectdreams.loritta.morenitta.utils.newconfig

import kotlinx.serialization.Serializable

@Serializable
data class BaseConfig(
    val loritta: LorittaConfig
)