package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils

import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.utils.math.MathUtils
import net.perfectdreams.loritta.cinnamon.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.cinnamon.utils.text.TextUtils.stripCodeBackticks
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.CalculatorCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits

class CalculatorExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val expression = string("expression", CalculatorCommand.I18N_PREFIX.Options.Expression) {
            cinnamonAutocomplete { context, focused ->
                val expression = focused.value

                val result = eval(expression)

                if (result != null) {
                    mapOf("= $result" to expression)
                } else {
                    val message = context.i18nContext.get(
                        CalculatorCommand.I18N_PREFIX.Invalid(
                            expression
                        )
                    ).stripCodeBackticks().shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length)

                    mapOf(message to expression)
                }
            }
        }
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val expression = args[options.expression]

        val result = eval(expression)

        if (result != null) {
            context.sendReply(
                content = context.i18nContext.get(
                    CalculatorCommand.I18N_PREFIX.Result(
                        result
                    )
                ),
                prefix = Emotes.LoriReading
            )

            if (expression.replace(" ", "") == "1+1")
                context.giveAchievementAndNotify(AchievementType.ONE_PLUS_ONE_CALCULATION)
        } else {
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

    private fun eval(expression: String): Double? {
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
                return (resultNumber2 * resultNumber1) / resultNumber0
            }

            // TODO: Evaluate based on the user's locale (or maybe find what locale they are using by figuring out the number?)
            return MathUtils.evaluate(expression)
        } catch (e: Exception) {
            return null
        }
    }
}