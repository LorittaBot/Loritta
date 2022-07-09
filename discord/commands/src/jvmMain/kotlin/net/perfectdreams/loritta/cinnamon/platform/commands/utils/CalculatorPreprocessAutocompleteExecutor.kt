package net.perfectdreams.loritta.cinnamon.platform.commands.utils

import net.perfectdreams.discordinteraktions.common.autocomplete.FocusedCommandOption
import net.perfectdreams.loritta.cinnamon.common.utils.math.MathUtils
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.stripCodeBackticks
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.AutocompleteContext
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.StringAutocompleteExecutor
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.StringAutocompleteExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandOptionsWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.CalculatorCommand

class CalculatorPreprocessAutocompleteExecutor : StringAutocompleteExecutor {
    companion object : StringAutocompleteExecutorDeclaration()

    override suspend fun onAutocomplete(context: AutocompleteContext, focusedOption: FocusedCommandOption): Map<String, String> {
        val expression = focusedOption.value

        try {
            // Regra de três:tm:
            if (expression.contains("---")) {
                val split = expression.split("/")
                val firstSide = split[0].split("---")
                val secondSide = split[1].split("---")
                val number0 = firstSide[0].trim()
                val number1 = firstSide[1].trim()

                val number2 = secondSide[0].trim()
                val number3 = secondSide[1].trim()

                val resultNumber0 = MathUtils.evaluate(number0)
                val resultNumber1 = MathUtils.evaluate(number1)
                val resultNumber2 = MathUtils.evaluate(number2)

                // resultNumber0 --- resultNumber1
                // resultNumber2 --- x
                return mapOf(
                    "= ${(resultNumber2 * resultNumber1) / resultNumber0}" to expression
                )
            }

            val result = MathUtils.evaluate(expression)

            return mapOf(
                "= $result" to expression
            )
        } catch (e: Exception) {
            val message = context.i18nContext.get(
                CalculatorCommand.I18N_PREFIX.Invalid(
                    focusedOption.value
                )
            ).stripCodeBackticks().shortenWithEllipsis(SlashCommandOptionsWrapper.MAX_OPTIONS_DESCRIPTION_LENGTH)

            return mapOf(
                message to focusedOption.value
            )
        }
    }
}