package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.declarations.UndertaleCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox.TextBoxOptionsData
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext

object TextBoxHelper {
    fun LocalizedApplicationCommandOptions.textBoxTextOption() = string("text", UndertaleCommand.I18N_TEXTBOX_PREFIX.Options.Text)

    suspend fun getInteractionDataAndFailIfItDoesNotExist(context: ComponentContext, interactionDataId: Long): TextBoxOptionsData {
        return Json.decodeFromJsonElement<TextBoxOptionsData>(
            context.loritta.services.interactionsData.getInteractionData(interactionDataId)
                ?: context.fail {
                    styled(
                        context.i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.DataIsMissing),
                        net.perfectdreams.loritta.cinnamon.emotes.Emotes.AnnoyingDog
                    )
                }
        )
    }
}