package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class MoneyCommand : AbstractCommand("money", listOf("dinheiro", "grana"), CommandCategory.UTILS) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["MONEY_DESCRIPTION"]
	}

	override fun getExample(): List<String> {
		return listOf("USD BRL", "USD BRL 5", "USD BRL 19.99")
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.size >= 2) {
			var multiply: Double? = 1.0
			if (context.args.size > 2) {
				multiply = context.strippedArgs[2].replace(",", ".").toDoubleOrNull()
			}

			if (multiply == null) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["INVALID_NUMBER", context.args[2]])
				return
			}

			val from = context.strippedArgs[0].toUpperCase()
			val to = context.strippedArgs[1].toUpperCase()

			val freeCurrencyConverterRequest = HttpRequest.get("http://free.currencyconverterapi.com/api/v5/convert?q=${from}_${to}")
			val currencyConverterJson = jsonParser.parse(freeCurrencyConverterRequest.body()).obj

			val foundInCurrencyConverter = currencyConverterJson["query"].nullObj?.get("count").nullInt ?: 0 > 0

			var value: Double? = null

			if (from == to) { // :rolling_eyes:
				value = 1.0
			} else {
				if (!foundInCurrencyConverter) {
					// Se tem erro, vamos tentar converter usando crypto, iremos pegar em USD e no "to"
					val crypto = jsonParser.parse(HttpRequest.get("https://min-api.cryptocompare.com/data/price?fsym=$from&tsyms=USD,$to").body()).obj

					if (crypto.has("Response")) {
						// damn
						context.reply(
								LoriReply(
										message = locale["MONEY_INVALID_CURRENCY"].msgFormat(from, listOf("USD").joinToString(transform = { "`$it`" })),
										prefix = Constants.ERROR
								)
						)
						return
					}

					val valueInUSD = crypto["USD"].double
					val valueInCustom = crypto[to].nullDouble

					value = valueInCustom

					/* if (!validCurrencies.contains(to) && valueInCustom != null) {
						value = valueInCustom
					} else if (to != "USD") {
						val rate = fixerCurrencies["rates"].obj[to].double

						value = valueInUSD * rate
					} */
				} else {
					// we use currency converter api now bois
					value = currencyConverterJson["results"]["${from}_${to}"]["val"].double

					if (value == null) {
						context.reply(
								LoriReply(
										message = locale["MONEY_INVALID_CURRENCY"].msgFormat(from, listOf("USD").joinToString(transform = { "`$it`" })),
										prefix = Constants.ERROR
								)
						)
						return
					}
				}
			}

			val df = DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
			df.maximumFractionDigits = 340 // 340 = DecimalFormat.DOUBLE_FRACTION_DIGITS

			context.reply(
					LoriReply(
							message = locale["MONEY_CONVERTED", multiply, from, to, df.format(value!! * multiply)],
							prefix = "\uD83D\uDCB5"
					)
			)
		} else {
			this.explain(context)
		}
	}
}