package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.platform.components.data.SingleUserComponentData

@Serializable
data class CoinFlipBetGlobalStartMatchmakingData(
    override val userId: Snowflake,
    val quantity: Long
) : SingleUserComponentData