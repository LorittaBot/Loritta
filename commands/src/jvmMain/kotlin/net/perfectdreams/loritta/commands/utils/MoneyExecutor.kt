package net.perfectdreams.loritta.commands.utils

import net.perfectdreams.loritta.commands.utils.declarations.MoneyCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.locale.LocaleStringData
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class MoneyExecutor(val emotes: Emotes, val ecbManager: ECBManager) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(MoneyExecutor::class) {
        object Options : CommandOptions() {
            val from = string("from", LocaleKeyData("TODO_FIX_THIS"))
                .also {
                    for (currencyId in MoneyCommand.currencyIds) {
                        it.choice(currencyId, LocaleKeyData(currencyId))
                    }
                }
                .register()

            val to = string("to", LocaleKeyData("TODO_FIX_THIS"))
                .also {
                    for (currencyId in MoneyCommand.currencyIds) {
                        it.choice(currencyId, LocaleKeyData(currencyId))
                    }
                }
                .register()

            val quantity = optionalInteger("quantity", LocaleKeyData("TODO_FIX_THIS"))
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val from = args[options.from]
        val to = args[options.to]
        val multiply = args[options.quantity] ?: 1
        val exchangeRates = ecbManager.getOrUpdateExchangeRates().await()

        val value: Double

        if (from == to) { // :rolling_eyes:
            value = 1.0
        } else {
            // Para calcular, devemos lembrar que a base é em EUR
            // Então, para converter, primeiro devemos converter a currency para EUR e depois para o target
            // Primeiro iremos verificar se existe no exchange rate
            // Por exemplo, se a gente colocar BRL, o "valueInEuros" será 5.5956
            val euroValueInCurrency = exchangeRates[from] ?: context.fail(
                prefix = emotes.error,
                content = context.locale["${MoneyCommand.LOCALE_PREFIX}.invalidCurrency", from, exchangeRates.keys.joinToString(transform = { "`$it`" })]
            ) { isEphemeral = true }

            val valueInEuro = 1 / euroValueInCurrency

            val endValueInEuros = exchangeRates[to] ?: context.fail(
                prefix = emotes.error,
                content = context.locale["${MoneyCommand.LOCALE_PREFIX}.invalidCurrency", from, exchangeRates.keys.joinToString(transform = { "`$it`" })]
            ) { isEphemeral = true }

            value = endValueInEuros * valueInEuro
        }

        val df = DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
        df.maximumFractionDigits = 340 // 340 = DecimalFormat.DOUBLE_FRACTION_DIGITS

        context.sendReply(
            prefix = "\uD83D\uDCB5",
            content = context.locale["${MoneyCommand.LOCALE_PREFIX}.converted", multiply, from, to, df.format(value * multiply)]
        )
    }
}