package net.perfectdreams.loritta.cinnamon.discord.utils.entitycache

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
data class PuddingGuildVoiceState(
    val channelId: Snowflake,
    val userId: Snowflake
)