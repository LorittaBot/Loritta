package net.perfectdreams.loritta.deviouscache.server.utils

import dev.kord.common.entity.Snowflake

data class GuildAndUserPair(
    val guildId: Snowflake,
    val userId: Snowflake
)