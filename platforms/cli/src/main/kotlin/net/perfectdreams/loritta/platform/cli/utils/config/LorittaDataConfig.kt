package net.perfectdreams.loritta.platform.cli.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class LorittaDataConfig(
    val type: String,
    val pudding: PuddingConfig?
)