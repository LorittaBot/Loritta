package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`

import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.declarations.RateCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.ContextStringToUserNameConverter
import net.perfectdreams.loritta.morenitta.LorittaBot

class RateHusbandoExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val husbando = string("husbando", RateCommand.I18N_PREFIX.WaifuHusbando.Options.Husbando)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        RateWaifuExecutor.executeGeneric(
            ContextStringToUserNameConverter.convert(context, args[options.husbando]),
            context,
            RateCommand.HUSBANDO_SINGULAR,
            RateCommand.HUSBANDO_PLURAL
        )
    }
}