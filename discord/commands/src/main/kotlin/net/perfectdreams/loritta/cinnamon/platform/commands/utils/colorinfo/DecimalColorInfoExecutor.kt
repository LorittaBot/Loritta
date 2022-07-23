package net.perfectdreams.loritta.cinnamon.platform.commands.utils.colorinfo

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.ColorInfoCommand
import java.awt.Color

class DecimalColorInfoExecutor(client: GabrielaImageServerClient) : ColorInfoExecutor(client) {
    companion object : SlashCommandExecutorDeclaration() {
        object Options : ApplicationCommandOptions() {
            val decimal = integer(
                "decimal",
                ColorInfoCommand.I18N_PREFIX.DecimalColorInfo.Options.Decimal.Text
            ).register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val decimal = args[Options.decimal].toInt()

        val color = Color(decimal)
        executeWithColor(context, color)
    }
}