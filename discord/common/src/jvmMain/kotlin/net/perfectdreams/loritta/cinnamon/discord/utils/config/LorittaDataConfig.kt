package net.perfectdreams.loritta.cinnamon.discord.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class LorittaDataConfig(
    val type: String,
    val pudding: PuddingConfig?
)