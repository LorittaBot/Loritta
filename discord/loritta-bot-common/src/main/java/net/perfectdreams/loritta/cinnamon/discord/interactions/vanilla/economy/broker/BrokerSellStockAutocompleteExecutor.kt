package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.broker

import net.perfectdreams.discordinteraktions.common.autocomplete.FocusedCommandOption
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.autocomplete.AutocompleteContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.autocomplete.CinnamonAutocompleteHandler
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.common.utils.LorittaBovespaBrokerUtils

class BrokerSellStockAutocompleteExecutor(loritta: LorittaCinnamon) : CinnamonAutocompleteHandler<String>(loritta) {
    override suspend fun handle(
        context: AutocompleteContext,
        focusedOption: FocusedCommandOption
    ): Map<String, String> {
        val userBoughtStocks = loritta.services.bovespaBroker.getUserBoughtStocks(context.sender.id.toLong())
            .map { it.ticker }
            .toSet()

        val results = LorittaBovespaBrokerUtils.trackedTickerCodes.filter {
           it.ticker in userBoughtStocks && it.ticker.startsWith(focusedOption.value, true)
        }

        return results.map {
            "${it.name} (${it.ticker})" to it.ticker.lowercase()
        }.take(25).toMap()
    }
}