package net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.BrokerBuyStockExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.BrokerInfoExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.BrokerPortfolioExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.BrokerSellStockExecutor

object BrokerCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Sonhos

    override fun declaration() = command(listOf("broker"), CommandCategory.ECONOMY, I18N_PREFIX.Description) {
        subcommand(listOf("info"), TodoFixThisData) {
            executor = BrokerInfoExecutor
        }

        subcommand(listOf("portfolio"), TodoFixThisData) {
            executor = BrokerPortfolioExecutor
        }

        subcommandGroup(listOf("stocks"), TodoFixThisData) {
            subcommand(listOf("buy"), TodoFixThisData) {
                executor = BrokerBuyStockExecutor
            }

            subcommand(listOf("sell"), TodoFixThisData) {
                executor = BrokerSellStockExecutor
            }
        }
    }
}