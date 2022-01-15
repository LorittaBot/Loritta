package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.common.utils.TransactionType
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.platform.components.selects.SelectMenuExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.selects.SelectMenuWithDataExecutor

class ChangeTransactionFilterSelectMenuExecutor(
    val loritta: LorittaCinnamon,
    val client: GabrielaImageServerClient
) : SelectMenuWithDataExecutor {
    companion object : SelectMenuExecutorDeclaration(ChangeTransactionFilterSelectMenuExecutor::class, ComponentExecutorIds.CHANGE_TRANSACTION_FILTER_SELECT_MENU_EXECUTOR)

    override suspend fun onSelect(
        user: User,
        context: ComponentContext,
        data: String,
        values: List<String>
    ) {
        context.deferUpdateMessage()

        val decoded = context.decodeViaComponentDataUtilsAndRequireUserToMatch<TransactionListData>(data)

        val builtMessage = TransactionsExecutor.createMessage(
            loritta,
            context.i18nContext,
            decoded.copy(
                page = 0, // Change the page to zero when changing the current filter
                transactionTypeFilter = values.map { TransactionType.valueOf(it) }
            )
        )

        context.updateMessage {
            apply(builtMessage)
        }
    }
}