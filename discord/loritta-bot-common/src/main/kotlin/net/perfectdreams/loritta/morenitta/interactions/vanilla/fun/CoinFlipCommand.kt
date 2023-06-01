package net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.commands.*

class CoinFlipCommand : SlashCommandDeclarationWrapper  {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Coinflip

        suspend fun executeCompat(context: CommandContextCompat) {
            val isTails = context.loritta.random.nextBoolean()
            val prefix: String
            val message: StringI18nData

            if (isTails) {
                prefix = Emotes.CoinTails.toString()
                message = I18N_PREFIX.Tails
            } else {
                prefix = Emotes.CoinHeads.toString()
                message = I18N_PREFIX.Heads
            }

            context.reply(false) {
                styled(
                    "**${context.i18nContext.get(message)}!**",
                    prefix
                )
            }
        }
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.FUN) {
        executor = CoinFlipExecutor()
    }

    inner class CoinFlipExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
            executeCompat(CommandContextCompat.InteractionsCommandContextCompat(context))
        }
    }
}