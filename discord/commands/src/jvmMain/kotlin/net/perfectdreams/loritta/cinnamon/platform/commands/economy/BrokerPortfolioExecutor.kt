package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.BrokerInfo.brokerBaseEmbed

class BrokerPortfolioExecutor : CommandExecutor() {
    companion object : CommandExecutorDeclaration(BrokerPortfolioExecutor::class)

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        context.deferChannelMessage()

        val stockInformations = context.loritta.services.bovespaBroker.getAllTickers()
        val userStockAssets = context.loritta.services.bovespaBroker.getUserBoughtStocks(context.user.id.value.toLong())

        context.sendMessage {
            brokerBaseEmbed {
                title = "Seu Portfólio de Ações"

                for (stockAsset in userStockAssets) {
                    val (tickerId, stockCount, stockSum, stockAverage) = stockAsset
                    val tickerName = BrokerInfo.trackedTickerCodes[stockAsset.ticker]
                    val tickerInformation = stockInformations.first { it.ticker == stockAsset.ticker }

                    // TODO: Fix this
                    val totalGainsIfSoldNow = BrokerInfo.convertToSellingPrice(
                        BrokerInfo.convertReaisToSonhos(
                            tickerInformation.value
                        )
                    ) * stockCount /* plugin.convertToSellingPrice(
                        plugin.convertReaisToSonhos(ticker[LoriBrokerPlugin.CURRENT_PRICE_FIELD]!!.jsonPrimitive.double)
                    ) * stockCount */

                    val diff = totalGainsIfSoldNow - stockSum
                    val emoji = when {
                        diff > 0 -> "\uD83D\uDD3C"
                        0 > diff -> "\uD83D\uDD3D"
                        else -> "⏹️"
                    }

                    // TODO: Fix this
                    val changePercentage = tickerInformation.dailyPriceVariation // ticker["chp"]?.jsonPrimitive?.double!!

                    field(
                        "$emoji `${tickerId}` ($tickerName) | ${"%.2f".format(changePercentage)}%",
                        "Você tem **$stockCount ações** neste ticker. Você gastou **$stockSum sonhos** neste ticker, se você vendesse todas as ações deste ticker agora, você ganharia **$totalGainsIfSoldNow sonhos (${diff.let { if (it > 0) "+$it" else it.toString() }})**",
                        true
                    )
                }
            }
        }
        /*
            // This may seem extremely dumb
			// "why don't you just do a groupBy and be happy???"
			// This causes issues where there is a LOT os rows loaded stuff in memory, so we need to do some dirty fixes to avoid that.
			// So we create two maps to store only the information that we want, and we avoid a .toList() causing a lot of objects to be stored in memory
			val totalStockExpensesById = mutableMapOf<String, Long>()
			val totalStockCountById = mutableMapOf<String, Long>()

			val stocks = loritta.newSuspendedTransaction {
				BoughtStocks.select {
					BoughtStocks.user eq user.idLong
				}.forEach {
					totalStockExpensesById[it[BoughtStocks.ticker]] = (totalStockExpensesById[it[BoughtStocks.ticker]] ?: 0) + it[BoughtStocks.price]
					totalStockCountById[it[BoughtStocks.ticker]] = (totalStockCountById[it[BoughtStocks.ticker]] ?: 0) + 1
				}
			}

			val embed = plugin.getBaseEmbed()
					.setTitle("${Emotes.LORI_STONKS} ${locale["commands.command.brokerportfolio.title"]}")

			for ((tickerId, totalSpent) in totalStockExpensesById) {
				val ticker = plugin.tradingApi
						.getOrRetrieveTicker(tickerId, listOf("lp", "description"))

				val tickerName = plugin.trackedTickerCodes[tickerId]

				val stockCount = totalStockCountById[tickerId] ?: 0

				val totalGainsIfSoldNow = plugin.convertToSellingPrice(
						plugin.convertReaisToSonhos(ticker[LoriBrokerPlugin.CURRENT_PRICE_FIELD]!!.jsonPrimitive.double)
				) * stockCount

				val diff = totalGainsIfSoldNow - totalSpent
				val emoji = when {
					diff > 0 -> "\uD83D\uDD3C"
					0 > diff -> "\uD83D\uDD3D"
					else -> "⏹️"
				}

				val changePercentage = ticker["chp"]?.jsonPrimitive?.double!!

				embed.addField(
						"$emoji `${ticker["short_name"]?.jsonPrimitive?.content}` ($tickerName) | ${"%.2f".format(changePercentage)}%",
						locale[
								"commands.command.brokerportfolio.youHaveStocksInThisTicker",
								stockCount,
								locale["commands.command.broker.stocks.${if (stockCount == 1L) "one" else "multiple"}"],
								totalSpent,
								totalGainsIfSoldNow,
								diff.let { if (it > 0) "+$it" else it.toString() }
						],
						true
				)
			}

			sendMessage(embed.build())
			*/
    }
}