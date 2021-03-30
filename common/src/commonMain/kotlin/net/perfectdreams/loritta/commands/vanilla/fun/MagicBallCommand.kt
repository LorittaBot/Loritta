package net.perfectdreams.loritta.commands.vanilla.`fun`

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.commands.vanilla.`fun`.declarations.MagicBallCommandDeclaration

class MagicBallCommand(val m: LorittaBot) : LorittaCommand<CommandContext>(MagicBallCommandDeclaration) {
    companion object {
        const val LOCALE_PREFIX = "commands.command.vieirinha"
    }

    override suspend fun executes(context: CommandContext) {
        // TODO: Fix Webhooks, maybe add a API to "impersonate" others that fallbacks to a normal message?
        context.sendMessage(context.locale.getList("$LOCALE_PREFIX.responses").random())
    }
}