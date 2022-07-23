package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import kotlin.random.Random

class CoinFlipExecutor(val random: Random) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val isTails = random.nextBoolean()
        val prefix: String
        val message: StringI18nData

        if (isTails) {
            prefix = Emotes.CoinTails.toString()
            message = CoinFlipCommand.I18N_PREFIX.Tails
        } else {
            prefix = Emotes.CoinHeads.toString()
            message = CoinFlipCommand.I18N_PREFIX.Heads
        }

        context.sendReply("**${context.i18nContext.get(message)}!**", prefix)
    }
}