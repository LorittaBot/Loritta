package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.transactions

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.entity.User
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.utils.LoadingEmojis
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.common.utils.TransactionType

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