package net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`

import net.dv8tion.jda.api.interactions.commands.Command
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference

class CoinFlipCommand : SlashCommandDeclarationWrapper  {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Coinflip
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.FUN) {
        enableLegacyMessageSupport = true
        this.integrationTypes = listOf(Command.IntegrationType.GUILD_INSTALL, Command.IntegrationType.USER_INSTALL)

        this.alternativeLegacyLabels.apply {
            add("girarmoeda")
            add("flipcoin")
            add("caracoroa")
        }

        executor = CoinFlipExecutor()
    }

    inner class CoinFlipExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
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

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> = LorittaLegacyMessageCommandExecutor.NO_ARGS
    }
}