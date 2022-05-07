package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.retribute

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.platform.components.data.SingleUserComponentData

@Serializable
data class RetributeRoleplayData(
    override val userId: Snowflake,
    val giver: Snowflake,
    val receiver: Snowflake
) : SingleUserComponentData