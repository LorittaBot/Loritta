package net.perfectdreams.loritta.cinnamon.platform.commands.utils.colorinfo

import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.ColorInfoCommand
import java.awt.Color

class HexColorInfoExecutor(loritta: LorittaCinnamon, client: GabrielaImageServerClient) : ColorInfoExecutor(loritta, client) {
    companion object {
        val HEX_PATTERN = "#?([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})".toPattern()
    }

    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val hex = string(
            "hex",
            ColorInfoCommand.I18N_PREFIX.HexColorInfo.Options.Hex.Text
        )
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val hex = args[options.hex]

        val hexMatcher = HEX_PATTERN.matcher(hex)

        if (!hexMatcher.find())
            context.failEphemerally {
                styled(
                    context.i18nContext.get(ColorInfoCommand.I18N_PREFIX.HexColorInfo.InvalidColor),
                    Emotes.Error
                )
            }

        executeWithColor(context, Color.decode("#" + hexMatcher.group(1)))
    }
}