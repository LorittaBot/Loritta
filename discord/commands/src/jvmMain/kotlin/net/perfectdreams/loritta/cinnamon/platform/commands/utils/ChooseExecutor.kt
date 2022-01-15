package net.perfectdreams.loritta.cinnamon.platform.commands.utils

import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.ChooseCommand

class ChooseExecutor() : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(ChooseExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val choices = stringList(
                "choice",
                ChooseCommand.I18N_PREFIX.Options.Choice,
                minimum = 2, maximum = 25
            ).register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
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