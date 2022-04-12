package net.perfectdreams.loritta.cinnamon.platform.commands.economy.broker

import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaBovespaBrokerUtils
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.broker.BrokerExecutorUtils.brokerBaseEmbed
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.BrokerCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments

class BrokerStockInfoExecutor : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(BrokerStockInfoExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val ticker = string("ticker", BrokerCommand.I18N_PREFIX.Stock.Options.Ticker.Text)
                .also {
                    LorittaBovespaBrokerUtils.trackedTickerCodes.toList().sortedBy { it.first }.forEach { (tickerId, tickerTitle) ->
                        it.choice(tickerId.lowercase(), "$tickerTitle ($tickerId)")
                    }
                }
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessageEphemerally()

        val tickerId = args[Options.ticker].uppercase()

        // This should *never* happen because the values are validated on Discord side BUT who knows
        if (tickerId !in LorittaBovespaBrokerUtils.validStocksCodes)
            context.failEphemerally(context.i18nContext.get(BrokerCommand.I18N_PREFIX.ThatIsNotAnValidStockTicker))

        val stockInformation = context.loritta.services.bovespaBroker.getTicker(tickerId)
            ?: context.failEphemerally(context.i18nContext.get(BrokerCommand.I18N_PREFIX.ThatIsNotAnValidStockTicker))

        val stockAsset = context.loritta.services.bovespaBroker.getUserBoughtStocks(context.user.id.value.toLong())
            .firstOrNull { it.ticker == tickerId }

        context.sendEphemeralMessage {
            brokerBaseEmbed(context) {
                title = "${Emotes.LoriStonks} ${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Stock.Embed.Title)}"

                // This is just like the "/broker portfolio" command
                // There is two alternatives however: If the user has stock, the output will be the same as the "/broker portfolio" command
                // If not, it will be just the buy/sell price
                val tickerInformation = stockInformation
                val tickerName = LorittaBovespaBrokerUtils.trackedTickerCodes[tickerInformation.ticker]
                val currentPrice = LorittaBovespaBrokerUtils.convertReaisToSonhos(tickerInformation.value)
                val buyingPrice = LorittaBovespaBrokerUtils.convertToBuyingPrice(currentPrice) // Buying price
                val sellingPrice = LorittaBovespaBrokerUtils.convertToSellingPrice(currentPrice) // Selling price
                val changePercentage = tickerInformation.dailyPriceVariation
                val emojiStatus = BrokerExecutorUtils.getEmojiStatusForTicker(tickerInformation)

                if (stockAsset != null) {
                    val (tickerId, stockCount, stockSum, stockAverage) = stockAsset

                    val totalGainsIfSoldNow = LorittaBovespaBrokerUtils.convertToSellingPrice(
                        LorittaBovespaBrokerUtils.convertReaisToSonhos(
                            tickerInformation.value
                        )
                    ) * stockCount

                    val diff = totalGainsIfSoldNow - stockSum
                    val emojiProfit = when {
                        diff > 0 -> "\uD83D\uDD3C"
                        0 > diff -> "\uD83D\uDD3D"
                        else -> "⏹️"
                    }

                    // https://percentage-change-calculator.com/
                    val profitPercentage = ((totalGainsIfSoldNow - stockSum.toDouble()) / stockSum)

                    val youHaveSharesInThisTickerMessage = context.i18nContext.get(
                        BrokerCommand.I18N_PREFIX.Portfolio.YouHaveSharesInThisTicker(
                            stockCount,
                            stockSum,
                            totalGainsIfSoldNow,
                            diff.let { if (it > 0) "+$it" else it.toString() },
                            profitPercentage
                        )
                    )

                    if (tickerInformation.status != LorittaBovespaBrokerUtils.MARKET) {
                        field(
                            "$emojiStatus$emojiProfit `${tickerId}` ($tickerName) | ${"%.2f".format(changePercentage)}%",
                            """${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.PriceBeforeMarketClose(currentPrice))}
                                |$youHaveSharesInThisTickerMessage
                            """.trimMargin()
                        )
                    } else {
                        field(
                            "$emojiStatus$emojiProfit `${tickerId}` ($tickerName) | ${"%.2f".format(changePercentage)}%",
                            """${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.BuyPrice(buyingPrice))}
                                |${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.SellPrice(sellingPrice))}
                                |$youHaveSharesInThisTickerMessage""".trimMargin()
                        )
                    }
                } else {
                    if (tickerInformation.status != LorittaBovespaBrokerUtils.MARKET) {
                        field(
                            "$emojiStatus `${tickerId}` ($tickerName) | ${"%.2f".format(changePercentage)}%",
                            context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.PriceBeforeMarketClose(currentPrice))
                                .trimMargin()
                        )
                    } else {
                        field(
                            "$emojiStatus `${tickerId}` ($tickerName) | ${"%.2f".format(changePercentage)}%",
                            """${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.BuyPrice(buyingPrice))}
                                |${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.SellPrice(sellingPrice))}""".trimMargin()
                        )
                    }
                }
            }
        }
    }
}