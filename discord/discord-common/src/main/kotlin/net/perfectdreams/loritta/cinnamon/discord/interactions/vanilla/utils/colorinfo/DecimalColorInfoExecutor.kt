package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.colorinfo

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.ColorInfoCommand
import java.awt.Color

class DecimalColorInfoExecutor(loritta: LorittaCinnamon, client: GabrielaImageServerClient) : ColorInfoExecutor(loritta, client) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val decimal = integer(
            "decimal",
            ColorInfoCommand.I18N_PREFIX.DecimalColorInfo.Options.Decimal.Text
        )
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val decimal = args[options.decimal].toInt()

        val color = Color(decimal)
        executeWithColor(context, color)
    }
}