package net.perfectdreams.loritta.cinnamon.platform.commands.images

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.SAMLogoRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.BRMemesCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments

class SAMExecutor(val client: GabrielaImageServerClient) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration() {
        object Options : ApplicationCommandOptions() {
            val type = string("type", BRMemesCommand.I18N_PREFIX.Sam.Options.Type)
                .choice("1", BRMemesCommand.I18N_PREFIX.Sam.Options.Choice.Sam1)
                .choice("2", BRMemesCommand.I18N_PREFIX.Sam.Options.Choice.Sam2)
                .choice("3", BRMemesCommand.I18N_PREFIX.Sam.Options.Choice.Sam3)
                .register()

            val imageReference = imageReference("image", BRMemesCommand.I18N_PREFIX.Sam.Options.Image)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val type = args[options.type]
        val imageReference = args[options.imageReference]

        val result = client.handleExceptions(context) {
            client.images.samLogo(
                SAMLogoRequest(
                    URLImageData(imageReference.url),
                    when (type) {
                        "1" -> SAMLogoRequest.LogoType.SAM_1
                        "2" -> SAMLogoRequest.LogoType.SAM_2
                        "3" -> SAMLogoRequest.LogoType.SAM_3
                        else -> error("Unsupported Logo Type!")
                    }
                )
            )
        }

        context.sendMessage {
            addFile("sam_logo.png", result.inputStream())
        }
    }
}