package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.videos

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.CocieloChavesRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.loritta.cinnamon.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments

class ChavesCocieloExecutor(
    loritta: LorittaCinnamon,
    val client: GabrielaImageServerClient
) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        // The description is replaced with "User, URL or Emote" so we don't really care that we are using "TodoFixThisData" here
        val friend1Image = imageReference("friend1")

        val friend2Image = imageReference("friend2")

        val friend3Image = imageReference("friend3")

        val friend4Image = imageReference("friend4")

        val friend5Image = imageReference("friend5")
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val friend1 = args[options.friend1Image].get(context)
        val friend2 = args[options.friend2Image].get(context)
        val friend3 = args[options.friend3Image].get(context)
        val friend4 = args[options.friend4Image].get(context)
        val friend5 = args[options.friend5Image].get(context)

        val result = client.handleExceptions(context) {
            client.videos.cocieloChaves(
                CocieloChavesRequest(
                    URLImageData(
                        friend1.url
                    ),
                    URLImageData(
                        friend2.url
                    ),
                    URLImageData(
                        friend3.url
                    ),
                    URLImageData(
                        friend4.url
                    ),
                    URLImageData(
                        friend5.url
                    )
                )
            )
        }

        context.sendMessage {
            addFile("cocielo_chaves.mp4", result.inputStream())
        }
    }
}