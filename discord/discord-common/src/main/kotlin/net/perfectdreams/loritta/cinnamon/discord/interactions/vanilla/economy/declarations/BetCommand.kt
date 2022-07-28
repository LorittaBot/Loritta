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
        val COINFLIP_GLOBAL_I18N_PREFIX = I18nKeysData.Commands.Command.Betcoinflipglobal
    }

    override fun declaration() = slashCommand("bet", CommandCategory.ECONOMY, TodoFixThisData) {
        subcommandGroup("coinflip", TodoFixThisData) {
            subcommand("global", COINFLIP_GLOBAL_I18N_PREFIX.Description) {
                executor = { CoinFlipBetGlobalExecutor(it) }
            }
        }

        subcommand("coinflipglobal", COINFLIP_GLOBAL_I18N_PREFIX.Description) {
            executor = { CoinFlipBetGlobalExecutor(it) }
        }
    }
}