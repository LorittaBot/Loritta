package net.perfectdreams.loritta.cinnamon.platform.commands.economy.broker

import net.perfectdreams.discordinteraktions.common.autocomplete.FocusedCommandOption
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.AutocompleteContext
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.StringAutocompleteExecutor
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.StringAutocompleteExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandOptionsWrapper
import net.perfectdreams.loritta.cinnamon.platform.utils.NumberUtils

class BrokerStockQuantityAutocompleteExecutor(val loritta: LorittaCinnamon) : StringAutocompleteExecutor {
    companion object : StringAutocompleteExecutorDeclaration(BrokerStockQuantityAutocompleteExecutor::class)

    override suspend fun onAutocomplete(context: AutocompleteContext, focusedOption: FocusedCommandOption): Map<String, String> {
        val currentInput = focusedOption.value

        val ticker = context.getArgument(BrokerBuyStockExecutor.options.ticker)
        val tickerInfo = loritta.services.bovespaBroker.getTicker(ticker.uppercase()) ?: error("User is trying to autocomplete \"$ticker\", but that ticker doesn't exist!")

        val quantity = NumberUtils.convertShortenedNumberToLong(context.i18nContext, currentInput) ?: return mapOf(
            context.i18nContext.get(
                I18nKeysData.Innercommands.InvalidNumber(currentInput)
            ).replace("`", "").shortenWithEllipsis(SlashCommandOptionsWrapper.MAX_OPTIONS_DESCRIPTION_LENGTH) to "invalid_number"
        )

        return mapOf(
            context.i18nContext.get(
                I18nKeysData.Innercommands.Innercommand.Innerbroker.SharesCountWithPrice(
                    quantity,
                    quantity * tickerInfo.value
                )
            ).shortenWithEllipsis(SlashCommandOptionsWrapper.MAX_OPTIONS_DESCRIPTION_LENGTH) to quantity.toString()
        )
    }
}