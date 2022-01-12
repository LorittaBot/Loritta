package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.common.utils.TransactionType
import net.perfectdreams.loritta.cinnamon.platform.components.data.SingleUserComponentData
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

@Serializable
data class ChangeTransactionPageData(
    override val userId: Snowflake,
    val viewingTransactionsOfUserId: UserId,
    val page: Long,
    val transactionTypeFilter: List<TransactionType>
) : SingleUserComponentData