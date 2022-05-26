package net.perfectdreams.loritta.cinnamon.platform.commands.discord.info

import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake

@kotlinx.serialization.Serializable
data class GuildMemberPermissionsData(
    val roles: List<Snowflake>,
    val permissions: Permissions,
    val color: Int?
)