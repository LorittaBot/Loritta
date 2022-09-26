package net.perfectdreams.loritta.cinnamon.discord.utils.entitycache

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
data class PuddingGuildMember(
    val id: Snowflake,
    val roles: List<Snowflake>
)