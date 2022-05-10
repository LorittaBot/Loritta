package net.perfectdreams.loritta.cinnamon.platform.commands.utils.colorinfo

import dev.kord.common.kColor
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.ColorInfoCommand
import java.awt.Color

class RgbColorInfoExecutor(client: GabrielaImageServerClient) : ColorInfoExecutor(client) {
    companion object : SlashCommandExecutorDeclaration() {
        object Options : ApplicationCommandOptions() {
            val red = integer(
                "red",
                ColorInfoCommand.I18N_PREFIX.RgbColorInfo.Options.Red.Text
            ).register()

            val green = integer(
                "green",
                ColorInfoCommand.I18N_PREFIX.RgbColorInfo.Options.Green.Text
            ).register()

            val blue = integer(
                "blue",
                ColorInfoCommand.I18N_PREFIX.RgbColorInfo.Options.Blue.Text
            ).register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val red = args[Options.red].toInt()
        val green = args[Options.green].toInt()
        val blue = args[Options.blue].toInt()

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