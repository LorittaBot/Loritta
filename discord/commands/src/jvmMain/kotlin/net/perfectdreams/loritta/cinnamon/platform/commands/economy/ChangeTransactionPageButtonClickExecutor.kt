package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import net.perfectdreams.discordinteraktions.api.entities.User
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.platform.components.buttons.ButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.buttons.ButtonClickExecutorDeclaration

class ChangeTransactionPageButtonClickExecutor(
    val loritta: LorittaCinnamon,
    val client: GabrielaImageServerClient
) : ButtonClickExecutor {
    companion object : ButtonClickExecutorDeclaration(
        ChangeTransactionPageButtonClickExecutor::class,
        ComponentExecutorIds.CHANGE_TRANSACTION_PAGE_BUTTON_EXECUTOR
    )

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        context.deferUpdateMessage()

        // We only want to "confirm" the user input (for roleplaying purposes, to avoid filling the chat with a lot of messages)
        // so we will just keep the file but remove the components
        //
        // We could also use ephemeral messages, but nah, sometimes people *want* to show it off to other people without confirming
        val decoded = context.decodeViaComponentDataUtilsAndRequireUserToMatch<ChangeTransactionPageData>(data)

        val builtMessage = TransactionsExecutor.createMessage(
            loritta,
            decoded
        )

        context.updateMessage {
            apply(builtMessage)
        }
    }
}