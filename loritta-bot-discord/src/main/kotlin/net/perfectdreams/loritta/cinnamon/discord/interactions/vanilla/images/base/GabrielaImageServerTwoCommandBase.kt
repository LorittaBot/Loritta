package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.base

import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.TwoImagesRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.LorittaBot

open class GabrielaImageServerTwoCommandBase(
    loritta: LorittaBot,
    val client: GabrielaImageServerClient,
    val block: suspend GabrielaImageServerClient.(TwoImagesRequest) -> (ByteArray),
    val fileName: String
) : CinnamonSlashCommandExecutor(loritta) {
    override val options = TwoImagesOptions(loritta)

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val imageReference1 = args[options.imageReference1].get(context)!!
        val imageReference2 = args[options.imageReference2].get(context)!!

        val result = client.handleExceptions(context) {
            block.invoke(
                client,
                TwoImagesRequest(
                    URLImageData(
                        imageReference1.url
                    ),
                    URLImageData(
                        imageReference2.url
                    )
                )
            )
        }

        context.sendMessage {
            addFile(fileName, result.inputStream())
        }
    }
}