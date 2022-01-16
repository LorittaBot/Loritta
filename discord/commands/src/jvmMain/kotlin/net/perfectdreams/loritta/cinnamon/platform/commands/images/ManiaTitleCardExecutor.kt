package net.perfectdreams.loritta.cinnamon.platform.commands.images

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.ManiaTitleCardRequest
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.SonicCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments

class ManiaTitleCardExecutor(val client: GabrielaImageServerClient) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(ManiaTitleCardExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val line1 = string("line1", SonicCommand.I18N_PREFIX.Maniatitlecard.Options.Line1)
                .register()

            val line2 = optionalString("line2", SonicCommand.I18N_PREFIX.Maniatitlecard.Options.Line1)
                .register()
        }

        override val options = Options
    }

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