package net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox

import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext

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

        loritta.services.interactionsData.deleteInteractionData(decoded.interactionDataId)

        context.updateMessage {
            content = ""
            components = mutableListOf()
        }
    }
}