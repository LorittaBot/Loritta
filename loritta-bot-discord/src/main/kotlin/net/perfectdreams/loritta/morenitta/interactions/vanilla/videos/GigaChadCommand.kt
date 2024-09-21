package net.perfectdreams.loritta.morenitta.interactions.vanilla.videos

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.GigaChadRequest
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationBuilder
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.addFileData
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import java.util.UUID

class GigaChadCommand(
    val gabriela: GabrielaImageServerClient
) : SlashCommandDeclarationWrapper {

    override fun command(): SlashCommandDeclarationBuilder =
        slashCommand(
            name = I18N_PREFIX.Label,
            description = I18N_PREFIX.Description,
            category = CommandCategory.VIDEOS,
            uniqueId = UUID.fromString("52de196f-4330-40b0-94e4-4ce540f19d55")
        ) {
            executor = Executor()
        }

    inner class Executor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val averageFanText = string("average_fan_text", I18N_PREFIX.Options.AverageFanText)
            val averageEnjoyerText = string("average_enjoyer_text", I18N_PREFIX.Options.AverageEnjoyerText)
        }

        override val options: Options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(ephemeral = false)

            val result = gabriela.handleExceptions(context) {
                gabriela.videos.gigaChad(
                    GigaChadRequest(
                        virginLine = args[options.averageFanText],
                        gigachadLine = args[options.averageEnjoyerText]
                    )
                )
            }

            context.reply(ephemeral = false) {
                addFileData("gigachad.mp4", result)
            }
        }
    }

    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Gigachad
    }
}