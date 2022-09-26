package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.roleplay.retribute

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.data.SingleUserComponentData

@Serializable
data class RetributeRoleplayData(
    override val userId: Snowflake,
    val giver: Snowflake,
    val receiver: Snowflake,
    val combo: Int
) : SingleUserComponentData