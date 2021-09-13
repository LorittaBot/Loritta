package net.perfectdreams.loritta.cinnamon.platform.commands

abstract class CommandExecutor {
    abstract suspend fun execute(context: ApplicationCommandContext, args: CommandArguments)
}