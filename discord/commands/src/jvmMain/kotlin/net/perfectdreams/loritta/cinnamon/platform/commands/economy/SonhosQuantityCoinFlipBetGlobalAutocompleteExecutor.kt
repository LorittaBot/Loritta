package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import net.perfectdreams.discordinteraktions.common.autocomplete.FocusedCommandOption
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.IntegerAutocompleteExecutor
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.IntegerAutocompleteExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.utils.NumberUtils
import java.text.NumberFormat

class SonhosQuantityCoinFlipBetGlobalAutocompleteExecutor : IntegerAutocompleteExecutor {
    companion object : IntegerAutocompleteExecutorDeclaration(SonhosQuantityCoinFlipBetGlobalAutocompleteExecutor::class)

    override suspend fun onAutocomplete(focusedOption: FocusedCommandOption): Map<String, Long> {
        val currentInput = focusedOption.value

        // TODO: Fix this to use the user's locale
        val trueNumber = NumberUtils.convertShortenedNumberToLong(
            NumberFormat.getNumberInstance(),
            currentInput
        )

        val trueNumberAsString = trueNumber.toString()

        val choices = mutableMapOf<String, Long>()
        for (quantity in CoinflipBetGlobalExecutor.QUANTITIES) {
            if (focusedOption.value.isEmpty() || quantity.toString().startsWith(trueNumberAsString)) {
                choices["$quantity sonhos"] = quantity.toLong()
            }
        }

        return choices
    }
}