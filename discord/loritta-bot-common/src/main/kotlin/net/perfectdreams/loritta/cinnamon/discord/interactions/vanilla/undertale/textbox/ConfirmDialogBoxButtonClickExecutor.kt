package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox

import dev.kord.core.entity.User
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext

class ConfirmDialogBoxButtonClickExecutor(
    loritta: LorittaCinnamon,
    val client: GabrielaImageServerClient
) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.CONFIRM_DIALOG_BOX_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        // We only want to "confirm" the user input (for roleplaying purposes, to avoid filling the chat with a lot of messages)
        // so we will just keep the file but remove the components
        //
        // We could also use ephemeral messages, but nah, sometimes people *want* to show it off to other people without confirming
        val decoded = context.decodeDataFromComponentAndRequireUserToMatch<SelectGenericData>()

        loritta.pudding.interactionsData.deleteInteractionData(decoded.interactionDataId)

        context.updateMessage {
            content = ""
            components = mutableListOf()
        }
    }
}