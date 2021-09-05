package net.perfectdreams.loritta.platform.discord.commands.discord

import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.discord.commands.DiscordCommandContext

abstract class DiscordCommandExecutor: CommandExecutor() {
    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        executeDiscord(context as DiscordCommandContext, args)
    }

    abstract suspend fun executeDiscord(context: DiscordCommandContext, args: CommandArguments)
}