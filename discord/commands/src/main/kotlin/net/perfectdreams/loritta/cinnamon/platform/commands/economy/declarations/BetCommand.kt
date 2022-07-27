package net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.bet.CoinFlipBetGlobalExecutor

class BetCommand(loritta: LorittaCinnamon) : CinnamonSlashCommandDeclarationWrapper(loritta) {
    companion object {
        val COINFLIP_GLOBAL_I18N_PREFIX = I18nKeysData.Commands.Command.Betcoinflipglobal
    }

    override fun declaration() = slashCommand("bet", CommandCategory.ECONOMY, TodoFixThisData) {
        subcommandGroup("coinflip", TodoFixThisData) {
            subcommand("global", COINFLIP_GLOBAL_I18N_PREFIX.Description) {
                executor = CoinFlipBetGlobalExecutor(loritta)
            }
        }

        subcommand("coinflipglobal", COINFLIP_GLOBAL_I18N_PREFIX.Description) {
            executor = CoinFlipBetGlobalExecutor(loritta)
        }
    }
}