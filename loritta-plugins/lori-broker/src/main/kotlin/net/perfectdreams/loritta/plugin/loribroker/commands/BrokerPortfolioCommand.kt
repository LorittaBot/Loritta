package net.perfectdreams.loritta.plugin.loribroker.commands

import com.mrpowergamerbr.loritta.Loritta
import kotlinx.serialization.json.content
import kotlinx.serialization.json.double
import net.perfectdreams.loritta.plugin.loribroker.LoriBrokerPlugin
import net.perfectdreams.loritta.plugin.loribroker.commands.base.DSLCommandBase
import net.perfectdreams.loritta.plugin.loribroker.tables.BoughtStocks
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.kotlin.utils.addToStdlib.sumByLong

object BrokerPortfolioCommand : DSLCommandBase {
	override fun command(plugin: LoriBrokerPlugin, loritta: Loritta) = create(
			loritta,
			plugin.aliases.flatMap { listOf("$it portfolio", "$it portfólio", "$it p") }
	) {
		description { it["commands.economy.brokerPortfolio.description"] }

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
					.setTitle(locale["commands.economy.brokerPortfolio.title"])

			for ((tickerId, totalSpent) in totalStockExpensesById) {
				val ticker = plugin.tradingApi
						.getOrRetrieveTicker(tickerId, listOf("lp", "description"))

				val tickerName = plugin.fancyTickerNames[tickerId]

				val stockCount = totalStockCountById[tickerId] ?: 0

				val totalGainsIfSoldNow = plugin.convertReaisToSonhos(ticker["lp"]!!.double) * stockCount
				val diff = totalGainsIfSoldNow - totalSpent
				val emoji = when {
					diff > 0 -> "\uD83D\uDD3C"
					0 > diff -> "\uD83D\uDD3D"
					else -> "⏹️"
				}

				embed.addField(
						"$emoji `${ticker["short_name"]?.content}` ($tickerName)",
						locale[
								"commands.economy.brokerPortfolio.youHaveStocksInThisTicker",
								stockCount,
								locale["commands.economy.broker.stocks.${if (stockCount == 1L) "one" else "multiple"}"],
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