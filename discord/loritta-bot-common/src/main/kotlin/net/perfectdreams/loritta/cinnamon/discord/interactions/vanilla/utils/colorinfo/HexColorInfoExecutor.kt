package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.colorinfo

import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.ColorInfoCommand
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import java.awt.Color

class HexColorInfoExecutor(loritta: LorittaBot) : ColorInfoExecutor(loritta) {
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