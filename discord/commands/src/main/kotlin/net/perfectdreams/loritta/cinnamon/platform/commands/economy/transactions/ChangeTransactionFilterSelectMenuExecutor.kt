package net.perfectdreams.loritta.cinnamon.platform.commands.economy.transactions

import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.common.utils.TransactionType
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.components.CinnamonSelectMenuExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.platform.components.SelectMenuExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentExecutorIds

class ChangeTransactionFilterSelectMenuExecutor(
    loritta: LorittaCinnamon
) : CinnamonSelectMenuExecutor(loritta) {
    companion object : SelectMenuExecutorDeclaration(ComponentExecutorIds.CHANGE_TRANSACTION_FILTER_SELECT_MENU_EXECUTOR)

    override suspend fun onSelect(
        user: User,
        context: ComponentContext,
        values: List<String>
    ) {
        context.deferUpdateMessage()

        val decoded = context.decodeDataFromComponentAndRequireUserToMatch<TransactionListData>()

        val builtMessage = TransactionsExecutor.createMessage(
            loritta,
            context.i18nContext,
            decoded.copy(
                page = 0, // Change the page to zero when changing the current filter
                transactionTypeFilter = values.map { TransactionType.valueOf(it) }
            )
        )

        context.updateMessage {
            builtMessage()
        }
    }
}