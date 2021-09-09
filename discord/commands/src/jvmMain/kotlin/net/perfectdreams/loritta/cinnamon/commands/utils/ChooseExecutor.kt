package net.perfectdreams.loritta.cinnamon.commands.utils

import net.perfectdreams.loritta.cinnamon.commands.utils.declarations.ChooseCommand
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.commands.options.CommandOptions

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

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val options = args[options.choices]

        context.sendReply(
            content = context.i18nContext.get(
                ChooseCommand.I18N_PREFIX.Result(
                    result = options.random(),
                    emote = Emotes.loriYay
                )
            ),
            prefix = Emotes.loriHm
        )
    }
}