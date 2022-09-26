package net.perfectdreams.loritta.cinnamon.discord.interactions.inviteblocker

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.data.SingleUserComponentData

@Serializable
data class ActivateInviteBlockerData(
    override val userId: Snowflake,
    val roleId: Snowflake
) : SingleUserComponentData