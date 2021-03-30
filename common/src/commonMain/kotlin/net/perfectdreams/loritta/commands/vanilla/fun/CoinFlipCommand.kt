package net.perfectdreams.loritta.commands.vanilla.`fun`

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.commands.vanilla.`fun`.declarations.CoinFlipCommandDeclaration

class CoinFlipCommand(val m: LorittaBot) : LorittaCommand<CommandContext>(CoinFlipCommandDeclaration) {
    companion object {
        const val LOCALE_PREFIX = "commands.command.flipcoin"
    }

    override suspend fun executes(context: CommandContext) {
        val isTails = m.random.nextBoolean()
        val prefix: String
        val message: String

        if (isTails) {
            prefix = "<:coroa:412586257114464259>"
            message = context.locale["$LOCALE_PREFIX.tails"]
        } else {
            prefix = "<:cara:412586256409559041>"
            message = context.locale["$LOCALE_PREFIX.heads"]
        }

        context.reply(
            LorittaReply(
                "**$message!**",
                prefix
            )
        )
    }
}