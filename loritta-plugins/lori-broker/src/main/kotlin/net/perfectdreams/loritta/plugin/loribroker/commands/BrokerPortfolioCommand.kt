package net.perfectdreams.loritta.plugin.loribroker.commands

import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.loribroker.LoriBrokerPlugin
import net.perfectdreams.loritta.plugin.loribroker.tables.BoughtStocks
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.select

class BrokerPortfolioCommand(val plugin: LoriBrokerPlugin) : DiscordAbstractCommandBase(plugin.loritta, plugin.aliases.flatMap { listOf("$it portfolio", "$it portfólio", "$it p") }, CommandCategory.ECONOMY) {
	override fun command() = create {
		localizedDescription("commands.command.brokerportfolio.description")

		executesDiscord {
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
		}
	}
}