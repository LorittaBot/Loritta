package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.msgFormat
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import org.jsoup.Jsoup
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class MoneyCommand : AbstractCommand("money", listOf("dinheiro", "grana"), CommandCategory.UTILS) {
	companion object {
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

	override fun getDescriptionKey() = LocaleKeyData("commands.command.money.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.money.examples")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.size >= 2) {
			var multiply: Double? = 1.0
			if (context.args.size > 2) {
				multiply = context.strippedArgs[2].replace(",", ".").toDoubleOrNull()
			}

			if (multiply == null) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["commands.invalidNumber", context.args[2]])
				return
			}

			val from = context.strippedArgs[0].toUpperCase()
			val to = context.strippedArgs[1].toUpperCase()

			val exchangeRates = getOrUpdateExchangeRates().await()

			var value: Double? = null

			if (from == to) { // :rolling_eyes:
				value = 1.0
			} else {
				// Para calcular, devemos lembrar que a base é em EUR
				// Então, para converter, primeiro devemos converter a currency para EUR e depois para o target
				// Primeiro iremos verificar se existe no exchange rate
				// Por exemplo, se a gente colocar BRL, o "valueInEuros" será 5.5956
				val euroValueInCurrency = exchangeRates[from] ?: run {
					context.reply(
                            LorittaReply(
                                    message = locale["commands.command.money.invalidCurrency"].msgFormat(from, exchangeRates.keys.joinToString(transform = { "`$it`" })),
                                    prefix = Constants.ERROR
                            )
					)
					return
				}

				val valueInEuro = 1 / euroValueInCurrency

				val endValueInEuros = exchangeRates[to] ?: run {
					context.reply(
                            LorittaReply(
                                    message = locale["commands.command.money.invalidCurrency"].msgFormat(to, exchangeRates.keys.joinToString(transform = { "`$it`" })),
                                    prefix = Constants.ERROR
                            )
					)
					return
				}

				value = endValueInEuros * valueInEuro
			}

			val df = DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
			df.maximumFractionDigits = 340 // 340 = DecimalFormat.DOUBLE_FRACTION_DIGITS

			context.reply(
                    LorittaReply(
                            message = locale["commands.command.money.converted", multiply, from, to, df.format(value * multiply)],
                            prefix = "\uD83D\uDCB5"
                    )
			)
		} else {
			this.explain(context)
		}
	}
}