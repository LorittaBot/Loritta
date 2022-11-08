package net.perfectdreams.loritta.deviousfun.utils

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.deviouscache.data.DeviousGuildData
import net.perfectdreams.loritta.deviouscache.data.LightweightSnowflake

@Serializable
data class DeviousGuildDataWrapper(
    val data: DeviousGuildData
)