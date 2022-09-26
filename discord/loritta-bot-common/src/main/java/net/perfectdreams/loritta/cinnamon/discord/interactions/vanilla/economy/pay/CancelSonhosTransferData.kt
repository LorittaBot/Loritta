package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.pay

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.data.SingleUserComponentData

@Serializable
data class CancelSonhosTransferData(
    override val userId: Snowflake,
    val interactionDataId: Long
) : SingleUserComponentData