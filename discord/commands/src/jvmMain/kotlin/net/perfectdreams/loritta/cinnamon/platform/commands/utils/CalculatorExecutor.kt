package net.perfectdreams.loritta.cinnamon.platform.commands.utils

import net.perfectdreams.loritta.cinnamon.common.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.math.MathUtils
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.CalculatorCommand

class CalculatorExecutor() : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(CalculatorExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val expression = string("expression", CalculatorCommand.I18N_PREFIX.Options.Expression)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val expression = args[options.expression]

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
                context.sendReply(
                    content = context.i18nContext.get(
                        CalculatorCommand.I18N_PREFIX.Result(
                            (resultNumber2 * resultNumber1) / resultNumber0
                        )
                    ),
                    prefix = Emotes.LoriReading
                )
                return
            }

            // TODO: Evaluate based on the user's locale (or maybe find what locale they are using by figuring out the number?)
            val result = MathUtils.evaluate(expression)

            context.sendReply(
                content = context.i18nContext.get(
                    CalculatorCommand.I18N_PREFIX.Result(
                        result
                    )
                ),
                prefix = Emotes.LoriReading
            )

            if (expression.replace(" ", "") == "1+1")
                context.giveAchievement(AchievementType.ONE_PLUS_ONE_CALCULATION)
        } catch (e: Exception) {
            // TODO: Fix stripCodeMarks
            context.failEphemerally(
                context.i18nContext.get(
                    CalculatorCommand.I18N_PREFIX.Invalid(
                        expression
                    )
                ),
                prefix = Emotes.LoriHm
            )
        }
    }
}