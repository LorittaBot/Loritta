package net.perfectdreams.loritta.cinnamon.platform.commands.images

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.ManiaTitleCardRequest
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.SonicCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments

class ManiaTitleCardExecutor(
    loritta: LorittaCinnamon,
    val client: GabrielaImageServerClient
) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val line1 = string("line1", SonicCommand.I18N_PREFIX.Maniatitlecard.Options.Line1)

        val line2 = optionalString("line2", SonicCommand.I18N_PREFIX.Maniatitlecard.Options.Line1)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val line1 = args[options.line1]
        val line2 = args[options.line2]

        val result = client.handleExceptions(context) {
            client.images.maniaTitleCard(
                ManiaTitleCardRequest(
                    line1,
                    line2
                )
            )
        }

        context.sendMessage {
            addFile("mania_title_card.png", result.inputStream())
        }
    }
}