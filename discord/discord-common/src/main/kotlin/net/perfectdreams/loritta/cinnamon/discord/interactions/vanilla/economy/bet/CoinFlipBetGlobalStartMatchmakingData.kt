package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.bet

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.data.SingleUserComponentData

@Serializable
data class CoinFlipBetGlobalStartMatchmakingData(
    override val userId: Snowflake,
    val quantity: Long
) : SingleUserComponentData