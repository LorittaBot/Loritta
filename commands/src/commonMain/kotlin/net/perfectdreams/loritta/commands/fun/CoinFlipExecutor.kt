package net.perfectdreams.loritta.commands.`fun`

import net.perfectdreams.loritta.commands.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.emotes.Emotes
import kotlin.random.Random

class CoinFlipExecutor(val emotes: Emotes, val random: Random) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(CoinFlipExecutor::class)

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val isTails = random.nextBoolean()
        val prefix: String
        val message: String

        if (isTails) {
            prefix = emotes.coinTails.toString()
            message = context.locale["${CoinFlipCommand.LOCALE_PREFIX}.tails"]
        } else {
            prefix = emotes.coinHeads.toString()
            message = context.locale["${CoinFlipCommand.LOCALE_PREFIX}.heads"]
        }

        context.sendReply("**$message!**", prefix)
    }
}