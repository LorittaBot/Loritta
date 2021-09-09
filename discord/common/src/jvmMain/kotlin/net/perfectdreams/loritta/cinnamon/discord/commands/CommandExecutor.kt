package net.perfectdreams.loritta.cinnamon.discord.commands

abstract class CommandExecutor {
    abstract suspend fun execute(context: CommandContext, args: CommandArguments)
}