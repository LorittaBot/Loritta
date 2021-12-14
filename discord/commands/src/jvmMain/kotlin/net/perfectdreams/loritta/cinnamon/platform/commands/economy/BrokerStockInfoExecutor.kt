package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import net.perfectdreams.loritta.cinnamon.common.utils.LorittaBovespaBrokerUtils
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.BrokerCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions

class BrokerStockInfoExecutor : CommandExecutor() {
    companion object : CommandExecutorDeclaration(BrokerStockInfoExecutor::class) {
        object Options : CommandOptions() {
            val ticker = string("ticker", TodoFixThisData)
                .also {
                    LorittaBovespaBrokerUtils.trackedTickerCodes.toList().sortedBy { it.first }.forEach { (tickerId, tickerTitle) ->
                        it.choice(tickerId.lowercase(), "$tickerTitle ($tickerId)")
                    }
                }
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        context.deferChannelMessageEphemerally()

        val tickerId = args[Options.ticker].uppercase()

        // This should *never* happen because the values are validated on Discord side BUT who knows
        if (tickerId !in LorittaBovespaBrokerUtils.validStocksCodes)
            context.failEphemerally(context.i18nContext.get(BrokerCommand.I18N_PREFIX.ThatIsNotAnValidStockTicker))

        val stockInformation = context.loritta.services.bovespaBroker.getTicker(tickerId)
            ?: context.failEphemerally(context.i18nContext.get(BrokerCommand.I18N_PREFIX.ThatIsNotAnValidStockTicker))

        context.sendEphemeralMessage {
            brokerBaseEmbed(context) {
                val tickerName = LorittaBovespaBrokerUtils.trackedTickerCodes[tickerId]
                val currentPrice = LorittaBovespaBrokerUtils.convertReaisToSonhos(stockInformation.value)
                val buyingPrice = LorittaBovespaBrokerUtils.convertToBuyingPrice(currentPrice) // Buying price
                val sellingPrice = LorittaBovespaBrokerUtils.convertToSellingPrice(currentPrice) // Selling price
                val changePercentage = stockInformation.dailyPriceVariation

                // TODO: Reuse BrokerInfoExecutor code
                // TODO: Emotes on the field title
                title = "`$tickerId` ($tickerName) | ${"%.2f".format(changePercentage)}%"

                // TODO: Constant and other statuses
                if (stockInformation.status != LorittaBovespaBrokerUtils.MARKET) {
                    description = context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.PriceBeforeMarketClose(currentPrice))
                } else {
                    description = """${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.BuyPrice(buyingPrice))}
                                |${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.SellPrice(sellingPrice))}
                            """.trimMargin()
                }
            }
        }
    }
}