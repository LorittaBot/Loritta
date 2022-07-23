package net.perfectdreams.loritta.cinnamon.platform.commands.images

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.MemeMakerRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.MemeMakerCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments

class MemeMakerExecutor(val client: GabrielaImageServerClient) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration() {
        object Options : ApplicationCommandOptions() {
            val line1 = string("line1", MemeMakerCommand.I18N_PREFIX.Options.Line1)
                .register()

            val line2 = optionalString("line2", MemeMakerCommand.I18N_PREFIX.Options.Line2)
                .register()

            val imageReference = imageReferenceOrAttachment("image", I18nKeysData.Commands.Category.Images.Options.Image)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val imageReference = args[options.imageReference]
        val line1 = args[options.line1]
        val line2 = args[options.line2]

        val result = client.handleExceptions(context) {
            client.images.memeMaker(
                MemeMakerRequest(
                    URLImageData(imageReference.url),
                    line1,
                    line2
                )
            )
        }

        context.sendMessage {
            addFile("meme_maker.png", result.inputStream())
        }
    }
}