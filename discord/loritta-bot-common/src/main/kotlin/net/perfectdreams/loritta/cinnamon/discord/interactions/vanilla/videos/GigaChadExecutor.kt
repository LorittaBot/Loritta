package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.videos

import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.GigaChadRequest
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.videos.declarations.GigaChadCommand
import net.perfectdreams.loritta.morenitta.LorittaBot

class GigaChadExecutor(loritta: LorittaBot, val client: GabrielaImageServerClient) :
    CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val averageFanText = string("average_fan_text", GigaChadCommand.I18N_PREFIX.Options.AverageFanText)

        val averageEnjoyerText = string("average_enjoyer_text", GigaChadCommand.I18N_PREFIX.Options.AverageEnjoyerText)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val averageFanText = args[options.averageFanText]
        val averageEnjoyerText = args[options.averageEnjoyerText]

        val result = client.handleExceptions(context) {
            client.videos.gigaChad(
                GigaChadRequest(averageFanText, averageEnjoyerText)
            )
        }

        context.sendMessage {
            addFile("gigachad.mp4", result.inputStream())
        }
    }
}