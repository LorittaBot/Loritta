package net.perfectdreams.loritta.cinnamon.platform.commands.undertale

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.declarations.UndertaleCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.TextBoxOptionsData
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext

object TextBoxHelper {
    fun ApplicationCommandOptions.textBoxTextOption() = string("text", UndertaleCommand.I18N_TEXTBOX_PREFIX.Options.Text)

    suspend fun getInteractionDataAndFailIfItDoesNotExist(context: ComponentContext, interactionDataId: Long): TextBoxOptionsData {
        return Json.decodeFromJsonElement<TextBoxOptionsData>(
            context.loritta.services.interactionsData.getInteractionData(interactionDataId)
                ?: context.fail {
                    styled(
                        context.i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.DataIsMissing),
                        Emotes.AnnoyingDog
                    )
                }
        )
    }
}