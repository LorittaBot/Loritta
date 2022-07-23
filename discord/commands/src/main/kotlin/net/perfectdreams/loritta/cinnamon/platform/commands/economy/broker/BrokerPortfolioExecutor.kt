package net.perfectdreams.loritta.cinnamon.platform.commands.economy.broker

import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaBovespaBrokerUtils
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.broker.BrokerExecutorUtils.brokerBaseEmbed
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.BrokerCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments

class BrokerPortfolioExecutor : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage()

        val stockInformations = context.loritta.services.bovespaBroker.getAllTickers()
        val userStockAssets = context.loritta.services.bovespaBroker.getUserBoughtStocks(context.user.id.value.toLong())

        if (userStockAssets.isEmpty())
            context.fail(
                context.i18nContext.get(BrokerCommand.I18N_PREFIX.Portfolio.YouDontHaveAnyShares),
                Emotes.LoriSob
            )

        context.sendMessage {
            brokerBaseEmbed(context) {
                title = "${Emotes.LoriStonks} ${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Portfolio.Title)}"

                val totalStockCount = userStockAssets.sumOf { it.count }
                val totalStockSum = userStockAssets.sumOf { it.sum }
                val totalGainsIfSoldEverythingNow = userStockAssets.sumOf { stockAsset ->
                    val tickerInformation = stockInformations.first { it.ticker == stockAsset.ticker }

                    LorittaBovespaBrokerUtils.convertToSellingPrice(
                        LorittaBovespaBrokerUtils.convertReaisToSonhos(
                            tickerInformation.value
                        )
                    ) * stockAsset.count
                }
                val diff = totalGainsIfSoldEverythingNow - totalStockSum
                val totalProfitPercentage = ((totalGainsIfSoldEverythingNow - totalStockSum.toDouble()) / totalStockSum)

                description = context.i18nContext.get(
                    BrokerCommand.I18N_PREFIX.Portfolio.YouHaveSharesInYourPortfolio(
                        totalStockCount,
                        totalStockSum,
                        totalGainsIfSoldEverythingNow,
                        diff.let { if (it > 0) "+$it" else it.toString() },
                        totalProfitPercentage
                    )
                )

                for (stockAsset in userStockAssets) {
                    val (tickerId, stockCount, stockSum, stockAverage) = stockAsset
                    val tickerName = LorittaBovespaBrokerUtils.trackedTickerCodes[stockAsset.ticker]
                    val tickerInformation = stockInformations.first { it.ticker == stockAsset.ticker }
                    val currentPrice = LorittaBovespaBrokerUtils.convertReaisToSonhos(tickerInformation.value)
                    val buyingPrice = LorittaBovespaBrokerUtils.convertToBuyingPrice(currentPrice) // Buying price
                    val sellingPrice = LorittaBovespaBrokerUtils.convertToSellingPrice(currentPrice) // Selling price
                    val emojiStatus = BrokerExecutorUtils.getEmojiStatusForTicker(tickerInformation)

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

                    val changePercentage = tickerInformation.dailyPriceVariation

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
                            """.trimMargin(),
                            true
                        )
                    } else {
                        field(
                            "$emojiStatus$emojiProfit `${tickerId}` ($tickerName) | ${"%.2f".format(changePercentage)}%",
                            """${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.BuyPrice(buyingPrice))}
                                |${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.SellPrice(sellingPrice))}
                                |$youHaveSharesInThisTickerMessage""".trimMargin(),
                            true
                        )
                    }
                }
            }
        }
    }
}