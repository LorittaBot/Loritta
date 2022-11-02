package net.perfectdreams.loritta.deviouscache.server.utils

import net.perfectdreams.loritta.deviouscache.data.LightweightSnowflake

data class GuildAndUserPair(
    val guildId: LightweightSnowflake,
    val userId: LightweightSnowflake
)