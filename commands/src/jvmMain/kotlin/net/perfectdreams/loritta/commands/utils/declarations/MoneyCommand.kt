package net.perfectdreams.loritta.commands.utils.declarations

import net.perfectdreams.loritta.commands.utils.MoneyExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object MoneyCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.money"

    // Remember, Discord Slash Commands have a limit of 25 options per command!
    // The overflown options are taken out before being registered
    val currencyIds = listOf(
        "EUR",
        "USD",
        "BRL",
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
        "CAD",
        "CNY",
        "HKD",
        "IDR",
        "ILS",
        "INR",
        "KRW",
        "MXN",
        "MYR",
        "NZD",
        "PHP",
        "SGD",
        "THB",
        "ZAR"
    )

    override fun declaration() = command(listOf("money", "dinheiro", "grana")) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = MoneyExecutor
    }
}