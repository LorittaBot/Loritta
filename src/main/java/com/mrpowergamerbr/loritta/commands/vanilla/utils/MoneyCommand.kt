package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat

class MoneyCommand : CommandBase() {
	override fun getLabel(): String {
		return "money"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.MONEY_DESCRIPTION
	}

	override fun getExample(): List<String> {
		return listOf("USD BRL", "USD BRL 5", "USD BRL 19.99")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS
	}

	override fun run(context: CommandContext) {
		if (context.args.size >= 2) {
			var multiply: Double? = 1.0;
			if (context.args.size > 2) {
				multiply = context.strippedArgs[2].replace(",", ".").toDoubleOrNull();
			}

			if (multiply == null) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.INVALID_NUMBER.msgFormat(context.args[2]))
				return;
			}

			val from = context.strippedArgs[0].toUpperCase()
			val to = context.strippedArgs[1].toUpperCase()

			val response = HttpRequest.get("http://api.fixer.io/latest?base=" + from).acceptJson().body()
			val validCurrenciesResponse = HttpRequest.get("http://api.fixer.io/latest").acceptJson().body()
			val fixerResponse = jsonParser.parse(response).asJsonObject // Base
			val validCurrResponse = jsonParser.parse(validCurrenciesResponse).asJsonObject // Valid Currencies

			val validCurrencies = validCurrResponse.get("rates").asJsonObject.entrySet().joinToString(transform = { "`${it.key}`" })

			if (fixerResponse.has("error")) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.MONEY_INVALID_CURRENCY.msgFormat(from, validCurrencies))
				return
			}
			val rates = fixerResponse.get("rates").asJsonObject
			if (!rates.has(to)) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.MONEY_INVALID_CURRENCY.msgFormat(to, validCurrencies))
				return
			}

			val converted = rates.get(to).asDouble
			context.sendMessage(context.getAsMention(true) + context.locale.MONEY_CONVERTED.msgFormat(multiply, from, to, (converted * multiply)))
		} else {
			this.explain(context)
		}
	}
}