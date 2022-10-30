package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images

import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.MemeMakerRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.declarations.MemeMakerCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.morenitta.LorittaBot

class MemeMakerExecutor(
    loritta: LorittaBot,
    val client: GabrielaImageServerClient
) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val line1 = string("line1", MemeMakerCommand.I18N_PREFIX.Options.Line1)

        val line2 = optionalString("line2", MemeMakerCommand.I18N_PREFIX.Options.Line2)

        val imageReference = imageReferenceOrAttachment("image")
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val imageReference = args[options.imageReference].get(context)!!
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