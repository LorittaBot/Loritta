package net.perfectdreams.loritta.morenitta.interactions.vanilla.utils

import dev.kord.common.Color
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.achievements.AchievementType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.math.MathUtils
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.common.utils.text.TextUtils.stripCodeBackticks
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions

class CalculatorCommand : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Calc

        suspend fun executeCompat(context: CommandContextCompat, expression: String) {
            val result = eval(expression)

            if (result != null) {
                context.reply(false) {
                    styled(
                        content = context.i18nContext.get(
                            I18N_PREFIX.Result(
                                result
                            )
                        ),
                        prefix = Emotes.LoriReading
                    )
                }

                if (expression.replace(" ", "") == "1+1")
                    context.giveAchievementAndNotify(AchievementType.ONE_PLUS_ONE_CALCULATION)
            } else {
                // TODO: Fix stripCodeMarks
                context.reply(true) {
                    styled(
                        content = context.i18nContext.get(
                            I18N_PREFIX.Invalid(
                                expression
                            )
                        ),
                        prefix = Emotes.LoriHm
                    )
                }
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

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.UTILS) {
        executor = CalculatorExecutor()
    }

    inner class CalculatorExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val expression = string("expression", I18N_PREFIX.Options.Expression) {
                autocomplete { context ->
                    val expression = context.event.focusedOption.value

                    if (expression.isBlank()) {
                        return@autocomplete mapOf(context.i18nContext.get(I18N_PREFIX.TypeArithmeticExpression) to "empty")
                    }

                    val result = eval(expression)

                    if (result != null) {
                        return@autocomplete mapOf("= $result" to expression)
                    } else {
                        val message = context.i18nContext.get(
                            I18N_PREFIX.Invalid(
                                expression
                            )
                        ).stripCodeBackticks().shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length)

                        return@autocomplete mapOf(message to expression)
                    }
                }
            }
        }

        override val options = Options()

        override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
            val expr = args[options.expression]
            if (expr == "empty") {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.YouNeedToTypeAnArithmeticExpression),
                        Emotes.LoriSleeping
                    )
                }
                return
            }

            executeCompat(CommandContextCompat.InteractionsCommandContextCompat(context), expr)
        }
    }
}