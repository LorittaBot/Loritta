package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import kotlin.random.Random

class CoinFlipExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val isTails = loritta.random.nextBoolean()
        val prefix: String
        val message: StringI18nData

        if (isTails) {
            prefix = net.perfectdreams.loritta.cinnamon.emotes.Emotes.CoinTails.toString()
            message = CoinFlipCommand.I18N_PREFIX.Tails
        } else {
            prefix = net.perfectdreams.loritta.cinnamon.emotes.Emotes.CoinHeads.toString()
            message = CoinFlipCommand.I18N_PREFIX.Heads
        }

        context.sendReply("**${context.i18nContext.get(message)}!**", prefix)
    }
}