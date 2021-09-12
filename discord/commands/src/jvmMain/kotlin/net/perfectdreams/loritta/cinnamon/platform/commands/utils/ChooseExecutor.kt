package net.perfectdreams.loritta.cinnamon.platform.commands.utils

import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.ChooseCommand
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions

class ChooseExecutor() : CommandExecutor() {
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

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        val options = args[options.choices]

        context.sendReply(
            content = context.i18nContext.get(
                ChooseCommand.I18N_PREFIX.Result(
                    result = options.random(),
                    emote = Emotes.LoriYay
                )
            ),
            prefix = Emotes.LoriHm
        )
    }
}