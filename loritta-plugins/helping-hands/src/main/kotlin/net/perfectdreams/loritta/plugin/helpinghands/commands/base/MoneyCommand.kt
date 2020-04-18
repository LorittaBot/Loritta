package net.perfectdreams.loritta.plugin.helpinghands.commands.base

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.plugin.helpinghands.HelpingHandsPlugin
import org.jsoup.Jsoup

class MoneyCommand : DSLCommandBase {
	var updatedAt = 0L
	var job: Deferred<Map<String, Double>>? = null

	fun getOrUpdateExchangeRates(): Deferred<Map<String, Double>> {
		val diff = System.currentTimeMillis() - updatedAt

		if (diff >= Constants.ONE_HOUR_IN_MILLISECONDS) {
			job = GlobalScope.async(loritta.coroutineDispatcher) {
				val jsoup = Jsoup.connect("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml?${System.currentTimeMillis()}")
						.get()

				val exchangeRates = jsoup.select("Cube").filter { it.hasAttr("currency") }
						.map { it.attr("currency") to it.attr("rate").toDouble() }
						.toMap()

				updatedAt = System.currentTimeMillis()

				exchangeRates
			}
		}

		return job!!
	}
	
	override fun command(plugin: HelpingHandsPlugin, loritta: LorittaBot) = create(
			loritta,
			listOf("money", "dinheiro", "grana", "exchange", "cambio", "câmbio")
	) {
		description { it["commands.utils.money.description"] }

		executes {

		}
	}
}