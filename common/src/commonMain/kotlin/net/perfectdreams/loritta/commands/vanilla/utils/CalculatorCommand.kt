package net.perfectdreams.loritta.commands.vanilla.utils

import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.commands.vanilla.utils.declarations.CalculatorCommandDeclaration
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.math.MathUtils

class CalculatorCommand : LorittaCommand<CommandContext>(CalculatorCommandDeclaration) {
    companion object {
        const val LOCALE_PREFIX = "commands.command.calc"
    }

    override suspend fun executes(context: CommandContext) {
        val expression = context.optionsManager.getString(CalculatorCommandDeclaration.options.expression)

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
                context.reply(
                    LorittaReply(
                        context.locale["$LOCALE_PREFIX.result", (resultNumber2 * resultNumber1) / resultNumber0]
                    )
                )
                return
            }

            val result = MathUtils.evaluate(expression)

            context.reply(
                LorittaReply(
                    context.locale["$LOCALE_PREFIX.result", result]
                )
            )
        } catch (e: Exception) {
            // TODO: Fix stripCodeMarks
            context.reply(
                LorittaReply(
                    context.locale["$LOCALE_PREFIX.invalid", expression] + " ${Emotes.LORI_CRYING}",
                    Emotes.LORI_HM
                )
            )
        }
    }
}