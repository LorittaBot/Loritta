package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.videos

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.FansExplainingRequest
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.videos.declarations.FansExplainingCommand

class FansExplainingExecutor(
    loritta: LorittaBot,
    val client: GabrielaImageServerClient
) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val section1Line1 = string("section1_line1", FansExplainingCommand.I18N_PREFIX.Options.Section1Line1)
        val section1Line2 = string("section1_line2", FansExplainingCommand.I18N_PREFIX.Options.Section1Line2)

        val section2Line1 = string("section2_line1", FansExplainingCommand.I18N_PREFIX.Options.Section2Line1)
        val section2Line2 = string("section2_line2", FansExplainingCommand.I18N_PREFIX.Options.Section2Line2)

        val section3Line1 = string("section3_line1", FansExplainingCommand.I18N_PREFIX.Options.Section3Line1)
        val section3Line2 = string("section3_line2", FansExplainingCommand.I18N_PREFIX.Options.Section3Line2)

        val section4Line1 = string("section4_line1", FansExplainingCommand.I18N_PREFIX.Options.Section4Line1)
        val section4Line2 = string("section4_line2", FansExplainingCommand.I18N_PREFIX.Options.Section4Line2)

        val section5Line1 = string("section5_line1", FansExplainingCommand.I18N_PREFIX.Options.Section5Line1)
        val section5Line2 = string("section5_line2", FansExplainingCommand.I18N_PREFIX.Options.Section5Line2)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val section1Line1 = args[options.section1Line1]
        val section1Line2 = args[options.section1Line2]

        val section2Line1 = args[options.section2Line1]
        val section2Line2 = args[options.section2Line2]

        val section3Line1 = args[options.section3Line1]
        val section3Line2 = args[options.section3Line2]

        val section4Line1 = args[options.section4Line1]
        val section4Line2 = args[options.section4Line2]

        val section5Line1 = args[options.section5Line1]
        val section5Line2 = args[options.section5Line2]

        val result = client.handleExceptions(context) {
            client.videos.fansExplaining(
                FansExplainingRequest(section1Line1, section1Line2, section2Line1, section2Line2, section3Line1, section3Line2, section4Line1, section4Line2, section5Line1, section5Line2)
            )
        }

        context.sendMessage {
            addFile("fans_explaining.mp4", result.inputStream())
        }
    }
}