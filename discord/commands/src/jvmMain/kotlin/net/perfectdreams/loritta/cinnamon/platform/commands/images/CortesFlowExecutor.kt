package net.perfectdreams.loritta.cinnamon.platform.commands.images

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.CortesFlowRequest
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.i18nhelper.core.keys.StringI18nKey
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.BRMemesCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments

class CortesFlowExecutor(val client: GabrielaImageServerClient) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration() {
        object Options : ApplicationCommandOptions() {
            val type = string("thumbnail", BRMemesCommand.I18N_PREFIX.Cortesflow.Options.Thumbnail)
                .also { option ->
                    BRMemesCommand.cortesFlowThumbnails.forEach {
                        option.choice(
                            it,
                            StringI18nData(
                                StringI18nKey("${BRMemesCommand.I18N_CORTESFLOW_KEY_PREFIX}.thumbnails.${TextUtils.kebabToLowerCamelCase(it)}"),
                                emptyMap()
                            )
                        )
                    }
                }
                .register()

            val text = string("text", BRMemesCommand.I18N_PREFIX.Cortesflow.Options.Text)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val type = args[options.type]
        val text = args[options.text]

        val result = client.handleExceptions(context) {
            client.images.cortesFlow(type, CortesFlowRequest(text))
        }

        context.sendMessage {
            addFile("cortes_flow.jpg", result.inputStream())
        }
    }
}