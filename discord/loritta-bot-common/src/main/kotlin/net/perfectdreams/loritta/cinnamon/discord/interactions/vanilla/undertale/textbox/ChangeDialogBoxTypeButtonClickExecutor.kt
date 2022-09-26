package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox

import dev.kord.core.entity.User
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.TextBoxExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.TextBoxHelper
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext

class ChangeDialogBoxTypeButtonClickExecutor(
    loritta: LorittaCinnamon,
    val client: GabrielaImageServerClient
) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.CHANGE_DIALOG_BOX_TYPE_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        // We will already defer to avoid issues
        // Also because we want to edit the message with a file... later!
        context.deferUpdateMessage()

        val (_, type, interactionDataId) = context.decodeDataFromComponentAndRequireUserToMatch<SelectDialogBoxTypeData>()

        val textBoxOptionsData = TextBoxHelper.getInteractionDataAndFailIfItDoesNotExist(context, interactionDataId)

        val newData = when (textBoxOptionsData) {
            is TextBoxWithCustomPortraitOptionsData -> textBoxOptionsData.copy(dialogBoxType = type)
            is TextBoxWithGamePortraitOptionsData -> textBoxOptionsData.copy(dialogBoxType = type)
            is TextBoxWithNoPortraitOptionsData -> textBoxOptionsData.copy(dialogBoxType = type)
        }

        // Delete the old interaction data ID from the database, the "createMessage" will create a new one anyways :)
        context.loritta.services.interactionsData.deleteInteractionData(interactionDataId)

        val builtMessage = TextBoxExecutor.createMessage(
            context.loritta,
            context.user,
            context.i18nContext,
            newData
        )

        val dialogBox = client.handleExceptions(context) { TextBoxExecutor.createDialogBox(client, newData) }

        context.updateMessage {
            attachments = mutableListOf() // Remove all attachments from the message!
            addFile("undertale_box.gif", dialogBox)
            apply(builtMessage)
        }
    }
}