package net.perfectdreams.loritta.commands.utils

import net.perfectdreams.loritta.commands.utils.declarations.ChooseCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes

class ChooseExecutor(val emotes: Emotes) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(ChooseExecutor::class) {
        object Options : CommandOptions() {
            val choices = stringList(
                "choice",
                ChooseCommand.I18N_PREFIX.Options.Choice,
                minimum = 2, maximum = 25
            ).register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val options = args[options.choices]

        context.sendReply(
            content = context.i18nContext.get(
                ChooseCommand.I18N_PREFIX.Result(
                    result = options.random(),
                    emote = emotes.loriYay
                )
            ),
            prefix = emotes.loriHm
        )
    }
}