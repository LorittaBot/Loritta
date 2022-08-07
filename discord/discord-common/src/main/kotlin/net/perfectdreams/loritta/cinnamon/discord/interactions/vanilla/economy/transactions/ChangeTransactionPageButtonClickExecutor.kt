package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.transactions

import dev.kord.core.entity.User
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds

class ChangeTransactionPageButtonClickExecutor(
    loritta: LorittaCinnamon
) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.CHANGE_TRANSACTION_PAGE_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        context.deferUpdateMessage()

        val decoded = context.decodeDataFromComponentAndRequireUserToMatch<TransactionListData>()

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