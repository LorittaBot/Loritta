package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.broker

import net.perfectdreams.discordinteraktions.common.autocomplete.FocusedCommandOption
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.autocomplete.AutocompleteContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.autocomplete.CinnamonAutocompleteHandler
import net.perfectdreams.loritta.cinnamon.utils.LorittaBovespaBrokerUtils

class BrokerBuyStockAutocompleteExecutor(loritta: LorittaCinnamon) : CinnamonAutocompleteHandler<String>(loritta) {
    override suspend fun handle(
        context: AutocompleteContext,
        focusedOption: FocusedCommandOption
    ): Map<String, String> {
        val results = LorittaBovespaBrokerUtils.trackedTickerCodes.filter {
            it.ticker.startsWith(focusedOption.value, true)
        }

        return results.map {
            "${it.name} (${it.ticker})" to it.ticker.lowercase()
        }.take(25).toMap()
    }
}