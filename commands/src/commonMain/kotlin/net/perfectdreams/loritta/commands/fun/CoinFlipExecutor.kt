package net.perfectdreams.loritta.commands.`fun`

import net.perfectdreams.loritta.commands.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import kotlin.random.Random

class CoinFlipExecutor : CommandExecutor() {
    companion object : CommandExecutorDeclaration(CoinFlipExecutor::class)

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val isTails = Random(0).nextBoolean()
        val prefix: String
        val message: String

        if (isTails) {
            prefix = "<:coroa:412586257114464259>"
            message = context.locale["${CoinFlipCommand.LOCALE_PREFIX}.tails"]
        } else {
            prefix = "<:cara:412586256409559041>"
            message = context.locale["${CoinFlipCommand.LOCALE_PREFIX}.heads"]
        }

        context.sendMessage("**$message!**")
    }
}