package net.perfectdreams.loritta.morenitta.commands

abstract class CommandExecutor {
    abstract suspend fun execute(
        context: CommandContext,
        args: net.perfectdreams.loritta.common.commands.CommandArguments
    )
}