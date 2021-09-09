package net.perfectdreams.loritta.cinnamon.commands.`fun`

import net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations.RollCommand
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.math.Dice
import net.perfectdreams.loritta.cinnamon.common.utils.math.MathUtils
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.discord.commands.styled
import kotlin.random.Random

class RollExecutor(val emotes: Emotes, val random: Random) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(RollExecutor::class) {
        object Options : CommandOptions() {
            val dices = optionalString("dices", RollCommand.I18N_PREFIX.Options.Dices)
                .register()

            val expression = optionalString("expression", RollCommand.I18N_PREFIX.Options.Expression)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val dicesAsString = args[options.dices]
        val mathematicalExpression = args[options.expression]

        val dices = try {
            // First we will parse only the dices, math expressions will be calculated later!
            val dices = Dice.parse(dicesAsString ?: "6", 100)

            if (dices.isEmpty())
                throw UnsupportedOperationException("No valid dices found!")

            dices
        } catch (e: Dice.Companion.TooManyDicesException) {
            context.failEphemerally(
                context.i18nContext.get(RollCommand.I18N_PREFIX.TooManyDices),
                emotes.loriSob
            )
        } catch (e: Dice.Companion.LowerBoundHigherThanUpperBoundException) {
            context.failEphemerally(
                context.i18nContext.get(RollCommand.I18N_PREFIX.InvalidBound),
                emotes.loriShrug
            )
        } catch (e: IllegalArgumentException) {
            context.failEphemerally(
                context.i18nContext.get(RollCommand.I18N_PREFIX.InvalidBound),
                emotes.loriShrug
            )
        } catch (e: UnsupportedOperationException) {
            context.failEphemerally(
                context.i18nContext.get(RollCommand.I18N_PREFIX.InvalidBound),
                emotes.loriShrug
            )
        }

        val rolledSides = mutableListOf<Long>()

        var response = ""
        for (dice in dices) {
            val rolledSide = random.nextLong(dice.lowerBound, dice.upperBound + 1)
            rolledSides.add(rolledSide)
        }

        response = rolledSides.joinToString(" + ")

        var finalResult = 0F

        rolledSides.forEach {
            finalResult += it
        }

        if (mathematicalExpression != null) {
            response += " = ${finalResult.toInt()} `${mathematicalExpression.trim()}"

            finalResult = MathUtils.evaluate(finalResult.toString() + mathematicalExpression).toFloat()

            response += " = ${finalResult.toInt()}`"
        }

        response = if (rolledSides.size == 1 && mathematicalExpression == null) {
            ""
        } else {
            "`${finalResult.toInt()}` **»** $response"
        }

        // All the dices have the same upper bound, so it doesn't matter what dice we choose
        val upperBound = dices.first().upperBound

        context.sendMessage {
            styled(
                context.i18nContext.get(
                    RollCommand.I18N_PREFIX.Result(
                        diceExpression = "${dices.size}d$upperBound",
                        result = finalResult.toInt()
                    )
                ),
                prefix = "\uD83C\uDFB2"
            )

            if (response.isNotEmpty())
                styled(content = response, prefix = "\uD83E\uDD13")
        }
    }
}