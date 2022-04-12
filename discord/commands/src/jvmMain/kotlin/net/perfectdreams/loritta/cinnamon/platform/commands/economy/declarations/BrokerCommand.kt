package net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.broker.BrokerBuyStockExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.broker.BrokerInfoExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.broker.BrokerPortfolioExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.broker.BrokerSellStockExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.broker.BrokerStockInfoExecutor

object BrokerCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Broker

    override fun declaration() = slashCommand(listOf("broker"), CommandCategory.ECONOMY, I18N_PREFIX.Description) {
        subcommand(listOf("info"), I18N_PREFIX.Info.Description) {
            executor = BrokerInfoExecutor
        }

        subcommand(listOf("portfolio"), I18N_PREFIX.Portfolio.Description) {
            executor = BrokerPortfolioExecutor
        }

        subcommand(listOf("stock"), I18N_PREFIX.Stock.Description) {
            executor = BrokerStockInfoExecutor
        }

        subcommand(listOf("buy"), I18N_PREFIX.Buy.Description) {
            executor = BrokerBuyStockExecutor
        }

        subcommand(listOf("sell"), I18N_PREFIX.Sell.Description) {
            executor = BrokerSellStockExecutor
        }
    }
}