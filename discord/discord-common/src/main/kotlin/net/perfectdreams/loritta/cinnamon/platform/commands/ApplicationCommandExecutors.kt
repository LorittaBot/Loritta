package net.perfectdreams.loritta.cinnamon.platform.commands

import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments

sealed class ApplicationCommandExecutor

abstract class SlashCommandExecutor : ApplicationCommandExecutor() {
    abstract suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments)
}