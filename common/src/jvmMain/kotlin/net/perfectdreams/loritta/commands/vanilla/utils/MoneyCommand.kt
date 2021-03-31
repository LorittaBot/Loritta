package net.perfectdreams.loritta.commands.vanilla.utils

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.commands.vanilla.utils.declarations.MoneyCommandDeclaration
import org.jsoup.Jsoup
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class MoneyCommand(val m: LorittaBot) : LorittaCommand<CommandContext>(MoneyCommandDeclaration) {
    companion object {
        const val LOCALE_PREFIX = "commands.command.money"

        val AVAILABLE_CURRENCIES = listOf(
            "USD",
            "JPY",
            "BGN",
            "CZK",
            "DKK",
            "GBP",
            "HUF",
            "PLN",
            "RON",
            "SEK",
            "CHF",
            "ISK",
            "NOK",
            "HRK",
            "RUB",
            "TRY",
            "AUD",
            "BRL",
            "CAD",
            "CNY",
            "HKD",
            "IDR",
            "ILS",
            "INR",
            "EUR"
            // "KRW",
            // "MXN",
            // "MYR",
            // "NZD",
            // "PHP",
            // "SGD",
            // "THB",
            // "ZAR"
        )
    }

    private var updatedAt = 0L
    private var job: Deferred<Map<String, Double>>? = null

    fun getOrUpdateExchangeRates(): Deferred<Map<String, Double>> {
        val diff = System.currentTimeMillis() - updatedAt

        // TODO: Fix Cooldown
        if (diff >= 0L) {
            // TODO: Fix async
            job = GlobalScope.async {
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

    override suspend fun executes(context: CommandContext) {
        // TODO: Fix Quantity
        var multiply: Double = 1.0

        val from = context.optionsManager.getString(MoneyCommandDeclaration.options.from)
        val to = context.optionsManager.getString(MoneyCommandDeclaration.options.to)

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
                        message = context.locale["commands.command.money.invalidCurrency", from, exchangeRates.keys.joinToString(transform = { "`$it`" })],
                        // TODO: Fix
                        // prefix = Constants.ERROR
                    )
                )
                return
            }

            val valueInEuro = 1 / euroValueInCurrency

            val endValueInEuros = exchangeRates[to] ?: run {
                context.reply(
                    LorittaReply(
                        message = context.locale["commands.command.money.invalidCurrency", to, exchangeRates.keys.joinToString(transform = { "`$it`" })],
                        // TODO: Fix
                        // prefix = Constants.ERROR
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
                message = context.locale["commands.command.money.converted", multiply, from, to, df.format(value * multiply)],
                prefix = "\uD83D\uDCB5"
            )
        )
    }
}