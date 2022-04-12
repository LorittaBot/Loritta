package net.perfectdreams.loritta.cinnamon.platform.commands.economy.transactions

import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickWithDataExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext

class ChangeTransactionPageButtonClickExecutor(
    val loritta: LorittaCinnamon
) : ButtonClickWithDataExecutor {
    companion object : ButtonClickExecutorDeclaration(
        ChangeTransactionPageButtonClickExecutor::class,
        ComponentExecutorIds.CHANGE_TRANSACTION_PAGE_BUTTON_EXECUTOR
    )

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        context.deferUpdateMessage()

        val decoded = context.decodeViaComponentDataUtilsAndRequireUserToMatch<TransactionListData>(data)

        val builtMessage = TransactionsExecutor.createMessage(
            loritta,
            context.i18nContext,
            decoded
        )

        context.updateMessage {
            builtMessage()
        }
    }
}