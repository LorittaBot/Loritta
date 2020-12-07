package net.perfectdreams.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.msgFormat
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import org.jsoup.Jsoup
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class MoneyCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("money", "dinheiro", "grana"), CommandCategory.UTILS) {
	companion object {
		private const val LOCALE_PREFIX = "commands.utils.money"
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
							.toMutableMap()

					exchangeRates["EUR"] = 1.0

					updatedAt = System.currentTimeMillis()

					exchangeRates
				}
			}

			return job!!
		}
	}

	override fun command() = create {
		localizedDescription("$LOCALE_PREFIX.description")

		localizedExamples("$LOCALE_PREFIX.examples")

		executesDiscord {
			val context = this

			if (context.args.size >= 2) {
				var multiply: Double? = 1.0
				if (context.args.size > 2) {
					multiply = context.args[2].replace(",", ".").toDoubleOrNull()
				}

				if (multiply == null) {
					context.reply(
							locale["commands.invalidNumber", context.args[2]],
							Constants.ERROR
					)
					return@executesDiscord
				}

				val from = context.args[0].toUpperCase()
				val to = context.args[1].toUpperCase()

				val exchangeRates = getOrUpdateExchangeRates().await()

				var value: Double? = null

				if (from == to) { // :rolling_eyes:
					value = 1.0
				} else {
					val euroValueInCurrency = exchangeRates[from] ?: run {
						context.reply(
								LorittaReply(
										message = locale["$LOCALE_PREFIX.invalidCurrency"].msgFormat(from, exchangeRates.keys.joinToString(transform = { "`$it`" })),
										prefix = Constants.ERROR
								)
						)
						return@executesDiscord
					}

					val valueInEuro = 1 / euroValueInCurrency

					val endValueInEuros = exchangeRates[to] ?: run {
						context.reply(
								LorittaReply(
										message = locale["$LOCALE_PREFIX.invalidCurrency"].msgFormat(to, exchangeRates.keys.joinToString(transform = { "`$it`" })),
										prefix = Constants.ERROR
								)
						)
						return@executesDiscord
					}

					value = endValueInEuros * valueInEuro
				}

				val df = DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
				df.maximumFractionDigits = 340 // 340 = DecimalFormat.DOUBLE_FRACTION_DIGITS

				context.reply(
						LorittaReply(
								message = locale["$LOCALE_PREFIX.converted", multiply, from, to, df.format(value * multiply)],
								prefix = "\uD83D\uDCB5"
						)
				)
			} else {
				explain()
			}
		}
	}
}