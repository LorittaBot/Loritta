package net.perfectdreams.loritta.deviouscache.server.utils

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.deviouscache.data.DeviousGuildData
import net.perfectdreams.loritta.deviouscache.data.LightweightSnowflake

@Serializable
data class DeviousGuildDataWrapper(
    val data: DeviousGuildData,
    val channelIds: Set<LightweightSnowflake>
)