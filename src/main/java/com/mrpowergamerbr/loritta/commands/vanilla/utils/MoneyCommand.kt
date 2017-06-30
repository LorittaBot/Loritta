package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils

class MoneyCommand : CommandBase() {
	override fun getLabel(): String {
		return "money"
	}

	override fun getDescription(): String {
		return "Transforma o valor de uma moeda em outra moeda. (Por exemplo: Ver quanto está valendo o dólar em relação ao real)"
	}

	override fun getExample(): List<String> {
		return listOf("USD BRL", "USD BRL 5", "USD BRL 19.99")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS
	}

	override fun run(context: CommandContext) {
		if (context.args.size >= 2) {
			var multiply: Double? = 0.0;
			if (context.args.size > 2) {
				multiply = context.args[2].replace(",", ".").toDoubleOrNull();
			}

			if (multiply == null) {
				context.sendMessage(LorittaUtils.ERROR + " | " + context.getAsMention(true) + "Número `${context.args[2]}` é algo irreconhecível para um bot como eu, sorry. \uD83D\uDE22")
				return;
			}

			val from = context.args[0].toUpperCase()
			val to = context.args[1].toUpperCase()

			val response = HttpRequest.get("http://api.fixer.io/latest?base=" + from).acceptJson().body()
			val validCurrenciesResponse = HttpRequest.get("http://api.fixer.io/latest").acceptJson().body()
			val fixerResponse = JsonParser().parse(response).asJsonObject // Base
			val validCurrResponse = JsonParser().parse(validCurrenciesResponse).asJsonObject // Valid Currencies

			val validCurrencies = validCurrResponse.get("rates").asJsonObject.entrySet().joinToString(transform = { "`${it.key}`" })

			if (fixerResponse.has("error")) {
				context.sendMessage(LorittaUtils.ERROR + " | " + context.getAsMention(true) + "`$from` não é uma moeda válida! 💸\n**Moedas válidas:** $validCurrencies")
				return
			}
			val rates = fixerResponse.get("rates").asJsonObject
			if (!rates.has(to)) {
				context.sendMessage(LorittaUtils.ERROR + " | " + context.getAsMention(true) + "`$to` não é uma moeda válida! 💸\n**Moedas válidas:** $validCurrencies")
				return
			}

			val converted = rates.get(to).asDouble
			context.sendMessage(context.getAsMention(true) + "💵 **$multiply " + from + " para " + to + ":** " + (converted * multiply) + " " + to)
		} else {
			this.explain(context.config, context.event)
		}
	}
}