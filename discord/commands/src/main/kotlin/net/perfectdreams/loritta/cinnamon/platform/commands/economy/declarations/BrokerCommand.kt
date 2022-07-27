package net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.broker.BrokerBuyStockExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.broker.BrokerInfoExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.broker.BrokerPortfolioExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.broker.BrokerSellStockExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.broker.BrokerStockInfoExecutor

class BrokerCommand(loritta: LorittaCinnamon) : CinnamonSlashCommandDeclarationWrapper(loritta) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Broker
    }

    override fun declaration() = slashCommand("broker", CommandCategory.ECONOMY, I18N_PREFIX.Description) {
        subcommand("info", I18N_PREFIX.Info.Description) {
            executor = BrokerInfoExecutor(loritta)
        }

        subcommand("portfolio", I18N_PREFIX.Portfolio.Description) {
            executor = BrokerPortfolioExecutor(loritta)
        }

        subcommand("stock", I18N_PREFIX.Stock.Description) {
            executor = BrokerStockInfoExecutor(loritta)
        }

        subcommand("buy", I18N_PREFIX.Buy.Description) {
            executor = BrokerBuyStockExecutor(loritta)
        }

        subcommand("sell", I18N_PREFIX.Sell.Description) {
            executor = BrokerSellStockExecutor(loritta)
        }
    }
}