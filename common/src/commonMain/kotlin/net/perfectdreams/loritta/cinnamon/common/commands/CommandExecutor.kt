package net.perfectdreams.loritta.cinnamon.common.commands

abstract class CommandExecutor {
    abstract suspend fun execute(context: CommandContext, args: CommandArguments)
}