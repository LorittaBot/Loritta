package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.transactions

import dev.kord.core.entity.User
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.*
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds

class ChangeTransactionPageButtonClickExecutor(
    loritta: LorittaBot
) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.CHANGE_TRANSACTION_PAGE_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        val decoded = context.decodeDataFromComponentAndRequireUserToMatch<ChangeTransactionPageData>()

        // Loading Section
        context.updateMessageSetLoadingState()

        val builtMessage = TransactionsExecutor.createMessage(
            loritta,
            context.i18nContext,
            decoded.userId,
            decoded.viewingTransactionsOfUserId,
            decoded.page,
            decoded.transactionTypeFilter
        )

        context.updateMessage {
            builtMessage()
        }
    }
}