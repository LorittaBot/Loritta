package net.perfectdreams.loritta.morenitta.interactions.vanilla.videos

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.FansExplainingRequest
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

class FansExplainingCommand(
    val gabriela: GabrielaImageServerClient
): SlashCommandDeclarationWrapper {
    override fun command(): SlashCommandDeclarationBuilder =
        slashCommand(
            name = I18N_PREFIX.Label,
            description = I18N_PREFIX.Description,
            category = CommandCategory.VIDEOS,
            uniqueId = UUID.fromString("9a6a80dd-d97a-4117-97e0-1bc93f2d5f74")
        ) {
            executor = Executor()
        }

    inner class Executor: LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val section1Line1 = string("celebrating_top", I18N_PREFIX.Options.Section1Line1)
            val section1Line2 = string("celebrating_bottom", I18N_PREFIX.Options.Section1Line2)

            val section2Line1 = string("explaining_top", I18N_PREFIX.Options.Section2Line1)
            val section2Line2 = string("explaining_bottom", I18N_PREFIX.Options.Section2Line2)

            val section3Line1 = string("exploding_top", I18N_PREFIX.Options.Section3Line1)
            val section3Line2 = string("exploding_bottom", I18N_PREFIX.Options.Section3Line2)

            val section4Line1 = string("waiting_top", I18N_PREFIX.Options.Section4Line1)
            val section4Line2 = string("waiting_bottom", I18N_PREFIX.Options.Section4Line2)

            val section5Line1 = string("raging_top", I18N_PREFIX.Options.Section5Line1)
            val section5Line2 = string("raging_bottom", I18N_PREFIX.Options.Section5Line2)
        }

        override val options: Options = Options()

        override suspend fun execute(
            context: UnleashedContext,
            args: SlashCommandArguments
        ) {
            context.deferChannelMessage(ephemeral = false)

            val result = gabriela.handleExceptions(context) {
                gabriela.videos.fansExplaining(
                    FansExplainingRequest(
                        section1Line1 = args[options.section1Line1],
                        section1Line2 = args[options.section1Line2],
                        section2Line1 = args[options.section2Line1],
                        section2Line2 = args[options.section2Line2],
                        section3Line1 = args[options.section3Line1],
                        section3Line2 = args[options.section3Line2],
                        section4Line1 = args[options.section4Line1],
                        section4Line2 = args[options.section4Line2],
                        section5Line1 = args[options.section5Line1],
                        section5Line2 = args[options.section5Line2]
                    )
                )
            }

            context.reply(ephemeral = false) {
                addFileData(
                    "fans_explaining.mp4",
                    result
                )
            }
        }
    }

    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Fansexplaining
    }
}