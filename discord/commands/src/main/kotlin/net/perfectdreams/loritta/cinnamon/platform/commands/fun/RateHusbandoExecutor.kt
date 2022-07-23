package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`

import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.RateCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.utils.ContextStringToUserNameConverter

class RateHusbandoExecutor() : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration() {
        object Options : ApplicationCommandOptions() {
            val husbando = string("husbando", RateCommand.I18N_PREFIX.WaifuHusbando.Options.Husbando)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        RateWaifuExecutor.executeGeneric(
            ContextStringToUserNameConverter.convert(context, args[options.husbando]),
            context,
            RateCommand.HUSBANDO_SINGULAR,
            RateCommand.HUSBANDO_PLURAL
        )
    }
}