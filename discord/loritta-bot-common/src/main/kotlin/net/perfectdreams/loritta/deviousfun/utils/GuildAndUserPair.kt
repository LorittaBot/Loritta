package net.perfectdreams.loritta.deviousfun.utils

import net.perfectdreams.loritta.deviouscache.data.LightweightSnowflake

data class GuildAndUserPair(
    val guildId: LightweightSnowflake,
    val userId: LightweightSnowflake
)