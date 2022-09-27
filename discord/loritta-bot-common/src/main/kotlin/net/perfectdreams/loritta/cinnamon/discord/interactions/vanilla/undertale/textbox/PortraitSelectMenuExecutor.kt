package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox

import dev.kord.core.entity.User
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.TextBoxExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.TextBoxHelper
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.CinnamonSelectMenuExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.SelectMenuExecutorDeclaration

class PortraitSelectMenuExecutor(
    loritta: LorittaBot,
    val client: GabrielaImageServerClient
) : CinnamonSelectMenuExecutor(loritta) {
    companion object : SelectMenuExecutorDeclaration(ComponentExecutorIds.PORTRAIT_SELECT_MENU_EXECUTOR)

    override suspend fun onSelect(
        user: User,
        context: ComponentContext,
        values: List<String>
    ) {
        // We will already defer to avoid issues
        // Also because we want to edit the message with a file... later!
        context.deferUpdateMessage()

        // Yes, this is unused because we haven't implemented buttons yet :(
        val (_, interactionDataId) = context.decodeDataFromComponentAndRequireUserToMatch<SelectGenericData>()

        val textBoxOptionsData = TextBoxHelper.getInteractionDataAndFailIfItDoesNotExist(context, interactionDataId)

        if (textBoxOptionsData !is TextBoxWithGamePortraitOptionsData)
            error("Trying to select a portrait while the type is not TextBoxWithGamePortraitOptionsData!")

        val newData = textBoxOptionsData.copy(portrait = values.first())

        // Delete the old interaction data ID from the database, the "createMessage" will create a new one anyways :)
        context.loritta.pudding.interactionsData.deleteInteractionData(interactionDataId)

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