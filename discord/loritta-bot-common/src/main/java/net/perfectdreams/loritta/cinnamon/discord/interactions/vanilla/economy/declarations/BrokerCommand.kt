package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations

import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.broker.BrokerBuyStockExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.broker.BrokerInfoExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.broker.BrokerPortfolioExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.broker.BrokerSellStockExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.broker.BrokerStockInfoExecutor

class BrokerCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Broker
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.ECONOMY, I18N_PREFIX.Description) {
        subcommand(I18N_PREFIX.Info.Label, I18N_PREFIX.Info.Description) {
            executor = { BrokerInfoExecutor(it) }
        }

        subcommand(I18N_PREFIX.Portfolio.Label, I18N_PREFIX.Portfolio.Description) {
            executor = { BrokerPortfolioExecutor(it) }
        }

        subcommand(I18N_PREFIX.Stock.Label, I18N_PREFIX.Stock.Description) {
            executor = { BrokerStockInfoExecutor(it) }
        }

        subcommand(I18N_PREFIX.Buy.Label, I18N_PREFIX.Buy.Description) {
            executor = { BrokerBuyStockExecutor(it) }
        }

        subcommand(I18N_PREFIX.Sell.Label, I18N_PREFIX.Sell.Description) {
            executor = { BrokerSellStockExecutor(it) }
        }
    }
}