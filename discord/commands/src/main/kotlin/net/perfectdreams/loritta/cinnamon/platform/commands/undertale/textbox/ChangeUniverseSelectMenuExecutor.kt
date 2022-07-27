package net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox

import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.commands.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.TextBoxExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.TextBoxHelper
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.characters.CharacterType
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.characters.UniverseType
import net.perfectdreams.loritta.cinnamon.platform.components.CinnamonSelectMenuExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.platform.components.SelectMenuExecutorDeclaration

class ChangeUniverseSelectMenuExecutor(
    loritta: LorittaCinnamon,
    val client: GabrielaImageServerClient
) : CinnamonSelectMenuExecutor(loritta) {
    companion object : SelectMenuExecutorDeclaration(ComponentExecutorIds.CHANGE_UNIVERSE_SELECT_MENU_EXECUTOR)

    override suspend fun onSelect(
        user: User,
        context: ComponentContext,
        values: List<String>
    ) {
        // We will already defer to avoid issues
        // Also because we want to edit the message with a file... later!
        context.deferUpdateMessage()

        val (_, interactionDataId) = context.decodeDataFromComponentAndRequireUserToMatch<SelectGenericData>()

        val textBoxOptionsData = TextBoxHelper.getInteractionDataAndFailIfItDoesNotExist(context, interactionDataId)

        if (textBoxOptionsData !is TextBoxWithUniverseOptionsData)
            error("Trying to select a universe while the type is not TextBoxWithUniverseOptionsData!")

        val newUniverse = UniverseType.valueOf(values.first())
        val newData = if (newUniverse == UniverseType.NONE) {
            TextBoxWithNoPortraitOptionsData(
                textBoxOptionsData.text,
                textBoxOptionsData.dialogBoxType
            )
        } else {
            TextBoxWithGamePortraitOptionsData(
                textBoxOptionsData.text,
                textBoxOptionsData.dialogBoxType,
                newUniverse,
                character = when (newUniverse) {
                    UniverseType.DELTARUNE -> CharacterType.DELTARUNE_RALSEI
                    UniverseType.UNDERTALE -> CharacterType.UNDERTALE_TORIEL
                    else -> error("Trying to select UniverseType.NONE while we are using TextBoxWithGamePortraitOptionsData!")
                },
                portrait = when (newUniverse) {
                    UniverseType.DELTARUNE -> "deltarune/ralsei/neutral"
                    // This is a workaround, if the UniverseType is "None", the character is not used at all
                    UniverseType.UNDERTALE -> "undertale/toriel/neutral"
                    else -> error("Trying to select UniverseType.NONE while we are using TextBoxWithGamePortraitOptionsData!")
                },
            )
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