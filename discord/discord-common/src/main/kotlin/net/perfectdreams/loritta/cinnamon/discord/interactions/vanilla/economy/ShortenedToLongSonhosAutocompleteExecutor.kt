package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy

import net.perfectdreams.discordinteraktions.common.autocomplete.FocusedCommandOption
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.autocomplete.AutocompleteContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.autocomplete.CinnamonAutocompleteHandler
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.discord.utils.NumberUtils
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.utils.text.TextUtils.shortenWithEllipsis

class ShortenedToLongSonhosAutocompleteExecutor(loritta: LorittaCinnamon) : CinnamonAutocompleteHandler<Long>(loritta) {
    override suspend fun handle(context: AutocompleteContext, focusedOption: FocusedCommandOption): Map<String, Long> {
        val currentInput = focusedOption.value

        val quantity = NumberUtils.convertShortenedNumberToLong(context.i18nContext, currentInput) ?: return mapOf()

        return mapOf(
            context.i18nContext.get(I18nKeysData.Commands.SonhosWithQuantity(quantity)).shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length) to quantity
        )
    }
}