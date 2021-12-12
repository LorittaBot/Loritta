package net.perfectdreams.loritta.cinnamon.platform.commands.moderation.ban

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.platform.components.data.SingleUserComponentData

@Serializable
data class UnbanData(
    override val userId: Snowflake,
    val bannedUserId: Snowflake
) : SingleUserComponentData