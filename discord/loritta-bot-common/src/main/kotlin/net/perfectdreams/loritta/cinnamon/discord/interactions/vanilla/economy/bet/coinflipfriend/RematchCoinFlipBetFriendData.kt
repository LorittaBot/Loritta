package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.bet.coinflipfriend

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.data.SingleUserComponentData

@Serializable
data class RematchCoinFlipBetFriendData(
    override val userId: Snowflake,
    val sourceId: Snowflake,
    val quantity: Long,
    val combo: Int
) : SingleUserComponentData