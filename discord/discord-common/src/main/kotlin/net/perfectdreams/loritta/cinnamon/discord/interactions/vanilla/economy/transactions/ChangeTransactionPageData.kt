package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.transactions

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.utils.TransactionType
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.data.SingleUserComponentData
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.xprank.ChangeXpRankPageData
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

@Serializable
data class ChangeTransactionPageData(
    override val userId: Snowflake,
    val viewingTransactionsOfUserId: Snowflake,
    val button: Button,
    val page: Long,
    val transactionTypeFilter: List<TransactionType>
) : SingleUserComponentData {
    enum class Button {
        LEFT_ARROW,
        RIGHT_ARROW,
        GO_TO_THE_LAST_PAGE
    }
}