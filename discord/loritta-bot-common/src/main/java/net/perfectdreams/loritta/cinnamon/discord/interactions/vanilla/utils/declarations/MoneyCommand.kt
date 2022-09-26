package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations

import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.utils.ecb.ECBManager
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.MoneyExecutor

class MoneyCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
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
            // "shekel" triggers Discord's bad words check, for some reason
            // https://canary.discord.com/channels/613425648685547541/916395737141620797/1022169074089861130
            // "ILS",
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
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.UTILS, I18N_PREFIX.Description) {
        executor = { MoneyExecutor(it, it.ecbManager) }
    }
}