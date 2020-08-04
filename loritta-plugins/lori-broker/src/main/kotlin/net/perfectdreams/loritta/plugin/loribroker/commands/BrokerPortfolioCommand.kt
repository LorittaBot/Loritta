package net.perfectdreams.loritta.plugin.loribroker.commands

import com.mrpowergamerbr.loritta.Loritta
import kotlinx.serialization.json.content
import kotlinx.serialization.json.double
import net.perfectdreams.loritta.plugin.loribroker.LoriBrokerPlugin
import net.perfectdreams.loritta.plugin.loribroker.commands.base.DSLCommandBase
import net.perfectdreams.loritta.plugin.loribroker.tables.BoughtStocks
import org.jetbrains.exposed.sql.select
import org.jetbrains.kotlin.utils.addToStdlib.sumByLong

object BrokerPortfolioCommand : DSLCommandBase {
	override fun command(plugin: LoriBrokerPlugin, loritta: Loritta) = create(
			loritta,
			plugin.aliases.flatMap { listOf("$it portfolio", "$it portfólio", "$it p") }
	) {
		description { it["commands.economy.brokerPortfolio.description"] }

		executesDiscord {
			val stocks = loritta.newSuspendedTransaction {
				BoughtStocks.select {
					BoughtStocks.user eq user.idLong
				}.toList()
			}

			val stockByTickerId = stocks.groupBy { it[BoughtStocks.ticker] }

			val embed = plugin.getBaseEmbed()
					.setTitle(locale["commands.economy.brokerPortfolio.title"])

			for ((tickerId, stocks) in stockByTickerId) {
				val ticker = plugin.tradingApi
						.getOrRetrieveTicker(tickerId, listOf("lp", "description"))

				val tickerName = plugin.fancyTickerNames[tickerId]

				val totalSpent = stocks.sumByLong { it[BoughtStocks.price] }
				val totalGainsIfSoldNow = plugin.convertReaisToSonhos(ticker["lp"]!!.double) * stocks.size
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
								stocks.size,
								locale["commands.economy.broker.stocks.${if (stocks.size == 1) "one" else "multiple"}"],
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