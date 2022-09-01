package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations

import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.bet.CoinFlipBetGlobalExecutor

class BetCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Bet
        val COINFLIP_GLOBAL_I18N_PREFIX = I18nKeysData.Commands.Command.Betcoinflipglobal
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.ECONOMY, TodoFixThisData) {
        subcommandGroup(I18nKeysData.Commands.Command.Coinflip.Label, TodoFixThisData) {
            subcommand(COINFLIP_GLOBAL_I18N_PREFIX.Label, COINFLIP_GLOBAL_I18N_PREFIX.Description) {
                executor = { CoinFlipBetGlobalExecutor(it) }
            }
        }

        subcommand(COINFLIP_GLOBAL_I18N_PREFIX.DiscordOldDiscordAppWorkaroundLabel, COINFLIP_GLOBAL_I18N_PREFIX.Description) {
            executor = { CoinFlipBetGlobalExecutor(it) }
        }
    }
}