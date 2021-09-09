package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`

import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.RateCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.platform.utils.ContextStringToUserNameConverter

class RateHusbandoExecutor() : CommandExecutor() {
    companion object : CommandExecutorDeclaration(RateHusbandoExecutor::class) {
        object Options : CommandOptions() {
            val husbando = string("husbando", RateCommand.I18N_PREFIX.WaifuHusbando.Options.Husbando)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        RateWaifuExecutor.executeGeneric(
            ContextStringToUserNameConverter.convert(context, args[RateWaifuExecutor.options.waifu]),
            context,
            RateCommand.HUSBANDO_SINGULAR,
            RateCommand.HUSBANDO_PLURAL
        )
    }
}