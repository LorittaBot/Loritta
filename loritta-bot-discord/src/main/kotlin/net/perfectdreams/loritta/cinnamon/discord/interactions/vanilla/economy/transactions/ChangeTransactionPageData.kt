package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.transactions

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.data.SingleUserComponentData

@Serializable
data class ChangeTransactionPageData(
    override val userId: Snowflake,
    val viewingTransactionsOfUserId: Snowflake,
    val page: Long,
    val transactionTypeFilter: List<TransactionType>
) : SingleUserComponentData