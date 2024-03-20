package net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`

import net.dv8tion.jda.api.interactions.commands.Command
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.math.Dice
import net.perfectdreams.loritta.common.utils.math.MathUtils
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.vanilla.utils.CalculatorCommand

class RollCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Roll
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.FUN) {
        enableLegacyMessageSupport = true

        alternativeLegacyLabels.apply {
            add("dice")
            add("dado")
        }
        this.integrationTypes = listOf(Command.IntegrationType.GUILD_INSTALL, Command.IntegrationType.USER_INSTALL)

        this.examples = I18N_PREFIX.Examples

        executor = RollExecutor()
    }

    inner class RollExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val dices = optionalString("dices", I18N_PREFIX.Options.Dices)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val dicesAndMathExpressionAsString = args[options.dices]
            val dicesAsString: String?
            val mathExpressionAsString: String?

            if (dicesAndMathExpressionAsString != null) {
                val split = dicesAndMathExpressionAsString.split(" ")
                dicesAsString = split[0]
                val mathExpressionAsStringSplit = split.drop(1)
                mathExpressionAsString = if (mathExpressionAsStringSplit.isEmpty())
                    null
                else
                    mathExpressionAsStringSplit.joinToString(" ")
            } else {
                dicesAsString = "6"
                mathExpressionAsString = null
            }

            val dices = try {
                // First we will parse only the dices, math expressions will be calculated later!
                val dices = Dice.parse(dicesAsString, 100)

                if (dices.isEmpty())
                    throw UnsupportedOperationException("No valid dices found!")

                dices
            } catch (e: Dice.Companion.TooManyDicesException) {
                context.fail(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.TooManyDices),
                        Emotes.LoriSob
                    )
                }
            } catch (e: Dice.Companion.LowerBoundHigherThanUpperBoundException) {
                context.fail(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.InvalidBound),
                        Emotes.LoriShrug
                    )
                }
            } catch (e: IllegalArgumentException) {
                context.fail(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.InvalidBound),
                        Emotes.LoriShrug
                    )
                }
            } catch (e: UnsupportedOperationException) {
                context.fail(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.InvalidBound),
                        Emotes.LoriShrug
                    )
                }
            }

            val rolledSides = mutableListOf<Long>()

            var response = ""
            for (dice in dices) {
                val rolledSide = loritta.random.nextLong(dice.lowerBound, dice.upperBound + 1)
                rolledSides.add(rolledSide)
            }

            response = rolledSides.joinToString(" + ")

            var finalResult = 0F

            rolledSides.forEach {
                finalResult += it
            }

            if (mathExpressionAsString != null) {
                try {
                    response += " = ${finalResult.toInt()} `${mathExpressionAsString.trim()}"

                    finalResult = MathUtils.evaluate(finalResult.toString() + mathExpressionAsString).toFloat()

                    response += " = ${finalResult.toInt()}`"
                } catch (e: Exception) {
                    // TODO: Fix stripCodeMarks
                    context.fail(true) {
                        styled(
                            context.i18nContext.get(
                                CalculatorCommand.I18N_PREFIX.Invalid(
                                    mathExpressionAsString
                                )
                            ),
                            prefix = Emotes.LoriHm
                        )
                    }
                }
            }

            response = if (rolledSides.size == 1 && mathExpressionAsString == null) {
                ""
            } else {
                "`${finalResult.toInt()}` **»** $response"
            }

            // All the dices have the same lower and upper bound, so it doesn't matter what dice we choose
            val firstDice = dices.first()
            val lowerBound = firstDice.lowerBound
            val upperBound = firstDice.upperBound

            context.reply(false) {
                styled(
                    context.i18nContext.get(
                        RollCommand.I18N_PREFIX.Result(
                            // Not showing the lower bound is confusing, because the user may think that the lower bound was never recognized
                            // So, if the lower bound is set, we will show it to the user!
                            diceExpression = if (lowerBound != 1L)
                                "${dices.size}d${lowerBound}..$upperBound"
                            else
                                "${dices.size}d$upperBound",
                            result = finalResult.toInt()
                        )
                    ),
                    prefix = Emotes.LoriGameDie
                )

                if (response.isNotEmpty())
                    styled(content = response, prefix = "\uD83E\uDD13")
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (context.args.isEmpty()) {
                context.explain()
                return null
            }

            return mapOf(
                options.dices to context.args.joinToString(" ")
            )
        }
    }
}