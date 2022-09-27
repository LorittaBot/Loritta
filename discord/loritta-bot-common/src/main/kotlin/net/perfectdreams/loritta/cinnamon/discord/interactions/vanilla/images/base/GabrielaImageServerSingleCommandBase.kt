package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.base

import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.SingleImageRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver.handleExceptions

open class GabrielaImageServerSingleCommandBase(
    loritta: LorittaBot,
    val client: GabrielaImageServerClient,
    val block: suspend GabrielaImageServerClient.(SingleImageRequest) -> (ByteArray),
    val fileName: String
) : CinnamonSlashCommandExecutor(loritta) {
    override val options = SingleImageOptions(loritta)

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val imageReference = args[options.imageReference].get(context)!!

        val result = client.handleExceptions(context) {
            block.invoke(
                client,
                SingleImageRequest(
                    URLImageData(
                        imageReference.url
                    )
                )
            )
        }

        context.sendMessage {
            addFile(fileName, result.inputStream())
        }
    }
}