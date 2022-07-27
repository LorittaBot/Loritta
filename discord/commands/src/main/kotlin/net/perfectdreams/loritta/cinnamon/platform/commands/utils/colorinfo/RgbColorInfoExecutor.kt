package net.perfectdreams.loritta.cinnamon.platform.commands.utils.colorinfo

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.ColorInfoCommand
import java.awt.Color

class RgbColorInfoExecutor(loritta: LorittaCinnamon, client: GabrielaImageServerClient) : ColorInfoExecutor(loritta, client) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val red = integer(
            "red",
            ColorInfoCommand.I18N_PREFIX.RgbColorInfo.Options.Red.Text
        )

        val green = integer(
            "green",
            ColorInfoCommand.I18N_PREFIX.RgbColorInfo.Options.Green.Text
        )

        val blue = integer(
            "blue",
            ColorInfoCommand.I18N_PREFIX.RgbColorInfo.Options.Blue.Text
        )
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val red = args[options.red].toInt()
        val green = args[options.green].toInt()
        val blue = args[options.blue].toInt()

        if (red !in 0..255 || green !in 0..255 || blue !in 0..255)
            context.failEphemerally {
                styled(
                    context.i18nContext.get(ColorInfoCommand.I18N_PREFIX.RgbColorInfo.InvalidColor),
                    Emotes.Error
                )
            }

        val color = Color(red, green, blue)
        executeWithColor(context, color)
    }
}