package net.perfectdreams.loritta.legacy.common.commands

abstract class CommandExecutor {
    abstract suspend fun execute(context: CommandContext, args: CommandArguments)
}