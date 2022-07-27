package net.perfectdreams.loritta.cinnamon.platform.commands.images

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.SAMLogoRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.BRMemesCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments

class SAMExecutor(loritta: LorittaCinnamon, val client: GabrielaImageServerClient) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val type = string("type", BRMemesCommand.I18N_PREFIX.Sam.Options.Type) {
            choice(BRMemesCommand.I18N_PREFIX.Sam.Options.Choice.Sam1, "1")
            choice(BRMemesCommand.I18N_PREFIX.Sam.Options.Choice.Sam2, "2")
            choice(BRMemesCommand.I18N_PREFIX.Sam.Options.Choice.Sam3, "3")
        }

        val imageReference = imageReferenceOrAttachment("image")
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val type = args[options.type]
        val imageReference = args[options.imageReference].get(context)!!

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