package net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.MoneyExecutor

object MoneyCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Money

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

    override fun declaration() = slashCommand(listOf("money", "dinheiro", "grana"), CommandCategory.UTILS, I18N_PREFIX.Description) {
        executor = MoneyExecutor
    }
}