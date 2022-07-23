package net.perfectdreams.loritta.cinnamon.platform.commands.utils.colorinfo

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.entities.LorittaReply
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.ColorInfoCommand
import java.awt.Color

class HexColorInfoExecutor(client: GabrielaImageServerClient) : ColorInfoExecutor(client) {
    companion object : SlashCommandExecutorDeclaration() {
        object Options : ApplicationCommandOptions() {
            val hex = string(
                "hex",
                ColorInfoCommand.I18N_PREFIX.HexColorInfo.Options.Hex.Text
            ).register()
        }

        override val options = Options

        val HEX_PATTERN = "#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})".toPattern()
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val hex = args[Options.hex]

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