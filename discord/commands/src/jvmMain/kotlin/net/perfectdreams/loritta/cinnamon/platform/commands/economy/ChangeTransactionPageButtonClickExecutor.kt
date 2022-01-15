package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.platform.components.buttons.ButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.buttons.ButtonClickExecutorDeclaration

class ChangeTransactionPageButtonClickExecutor(
    val loritta: LorittaCinnamon
) : ButtonClickExecutor {
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
            apply(builtMessage)
        }
    }
}