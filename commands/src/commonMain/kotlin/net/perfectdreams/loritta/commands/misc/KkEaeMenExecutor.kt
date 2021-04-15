package net.perfectdreams.loritta.commands.misc

import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.emotes.Emotes

class KkEaeMenExecutor(val emotes: Emotes) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(KkEaeMenExecutor::class)

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        context.sendMessage("kk eae ${emotes.vinDiesel}")
    }
}