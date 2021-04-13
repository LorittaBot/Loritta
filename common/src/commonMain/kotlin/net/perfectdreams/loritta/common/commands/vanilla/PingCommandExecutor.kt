package net.perfectdreams.loritta.common.commands.vanilla

import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration

class PingCommandExecutor : CommandExecutor() {
    companion object : CommandExecutorDeclaration(PingCommandExecutor::class)

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        context.sendMessage("Pong!")
    }
}