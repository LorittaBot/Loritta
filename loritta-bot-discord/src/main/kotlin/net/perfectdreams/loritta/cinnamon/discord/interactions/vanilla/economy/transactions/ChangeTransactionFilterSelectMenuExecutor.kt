package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.transactions

import dev.kord.core.entity.User
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.CinnamonSelectMenuExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.SelectMenuExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds

class ChangeTransactionFilterSelectMenuExecutor(
    loritta: LorittaBot
) : CinnamonSelectMenuExecutor(loritta) {
    companion object : SelectMenuExecutorDeclaration(ComponentExecutorIds.CHANGE_TRANSACTION_FILTER_SELECT_MENU_EXECUTOR)

    override suspend fun onSelect(
        user: User,
        context: ComponentContext,
        values: List<String>
    ) {
        val decoded = context.decodeDataFromComponentAndRequireUserToMatch<ChangeTransactionFilterData>()

        // Loading Section
        context.updateMessageSetLoadingState()

        val builtMessage = TransactionsExecutor.createMessage(
            loritta,
            context.i18nContext,
            context.user.id,
            decoded.viewingTransactionsOfUserId,
            0, // Change the page to zero when changing the current filter
            values.map { TransactionType.valueOf(it) }
        )

        context.updateMessage {
            builtMessage()
        }
    }
}