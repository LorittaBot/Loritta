package net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox

import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.commands.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.TextBoxExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.TextBoxHelper
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.characters.CharacterType
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.platform.components.selects.SelectMenuExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.selects.SelectMenuWithDataExecutor

class ChangeCharacterSelectMenuExecutor(
    val loritta: LorittaCinnamon,
    val client: GabrielaImageServerClient
) : SelectMenuWithDataExecutor {
    companion object : SelectMenuExecutorDeclaration(ChangeCharacterSelectMenuExecutor::class, ComponentExecutorIds.CHANGE_TOBY_CHARACTER_MENU_EXECUTOR)

    override suspend fun onSelect(user: User, context: ComponentContext, data: String, values: List<String>) {
        // We will already defer to avoid issues
        // Also because we want to edit the message with a file... later!
        context.deferUpdateMessage()

        val (_, interactionDataId) = context.decodeViaComponentDataUtilsAndRequireUserToMatch<SelectGenericData>(data)

        val textBoxOptionsData = TextBoxHelper.getInteractionDataAndFailIfItDoesNotExist(context, interactionDataId)

        if (textBoxOptionsData !is TextBoxWithGamePortraitOptionsData)
            error("Trying to select a universe while the type is not TextBoxWithGamePortraitOptionsData!")

        val character = CharacterType.valueOf(values.first())
        val newData = textBoxOptionsData.copy(
            character = character,
            portrait = character.defaultKeyName
        )

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