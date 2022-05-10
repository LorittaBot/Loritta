package net.perfectdreams.loritta.cinnamon.platform.commands.undertale

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.TextBoxHelper.textBoxTextOption
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.ColorPortraitType
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.DialogBoxType
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.TextBoxWithCustomPortraitOptionsData

class CustomTextBoxExecutor(val client: GabrielaImageServerClient) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration() {
        object Options : ApplicationCommandOptions() {
            val text = textBoxTextOption()
                .register()

            val imageReference = imageReference("image", TodoFixThisData)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val imageReference = args[Options.imageReference]
        val text = args[Options.text]

        val data = TextBoxWithCustomPortraitOptionsData(
            text,
            DialogBoxType.DARK_WORLD,
            imageReference.url,
            ColorPortraitType.COLORED
        )

        val builtMessage = TextBoxExecutor.createMessage(
            context.loritta,
            context.user,
            context.i18nContext,
            data
        )

        val dialogBox = TextBoxExecutor.createDialogBox(client, data)
        context.sendMessage {
            addFile("undertale_box.gif", dialogBox)
            apply(builtMessage)
        }
    }
}