package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.transactions

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.utils.TransactionType
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.data.SingleUserComponentData
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

@Serializable
data class TransactionListData(
    override val userId: Snowflake,
    val viewingTransactionsOfUserId: UserId,
    val page: Long,
    val transactionTypeFilter: List<TransactionType>
) : SingleUserComponentData