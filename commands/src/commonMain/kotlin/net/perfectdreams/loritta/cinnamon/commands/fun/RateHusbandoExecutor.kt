package net.perfectdreams.loritta.cinnamon.commands.`fun`

import net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations.RateCommand
import net.perfectdreams.loritta.cinnamon.common.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.common.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.common.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.common.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.InputConverter

class RateHusbandoExecutor(val emotes: Emotes, val inputConverter: InputConverter<String, String>) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(RateHusbandoExecutor::class) {
        object Options : CommandOptions() {
            val husbando = string("husbando", RateCommand.I18N_PREFIX.WaifuHusbando.Options.Husbando)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        RateWaifuExecutor.executeGeneric(
            inputConverter.convert(context, args[options.husbando]),
            context,
            emotes,
            RateCommand.HUSBANDO_SINGULAR,
            RateCommand.HUSBANDO_PLURAL
        )
    }
}