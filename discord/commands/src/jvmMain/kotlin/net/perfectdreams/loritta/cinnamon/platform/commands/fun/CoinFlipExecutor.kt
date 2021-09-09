package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import kotlin.random.Random

class CoinFlipExecutor(val random: Random) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(CoinFlipExecutor::class)

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val isTails = random.nextBoolean()
        val prefix: String
        val message: StringI18nData

        if (isTails) {
            prefix = Emotes.coinTails.toString()
            message = CoinFlipCommand.I18N_PREFIX.Tails
        } else {
            prefix = Emotes.coinHeads.toString()
            message = CoinFlipCommand.I18N_PREFIX.Heads
        }

        context.sendReply("**${context.i18nContext.get(message)}!**", prefix)
    }
}