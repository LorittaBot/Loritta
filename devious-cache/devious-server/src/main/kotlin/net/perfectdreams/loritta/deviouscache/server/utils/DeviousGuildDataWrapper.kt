package net.perfectdreams.loritta.deviouscache.server.utils

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.deviouscache.data.DeviousGuildData

@Serializable
data class DeviousGuildDataWrapper(
    val data: DeviousGuildData,
    val channelIds: Set<Snowflake>
)