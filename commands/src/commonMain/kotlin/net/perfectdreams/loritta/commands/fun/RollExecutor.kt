package net.perfectdreams.loritta.commands.`fun`

import net.perfectdreams.loritta.commands.`fun`.declarations.RollCommand
import net.perfectdreams.loritta.commands.utils.declarations.CalculatorCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.math.Dice
import net.perfectdreams.loritta.common.utils.math.MathUtils
import kotlin.random.Random

class RollExecutor(val emotes: Emotes, val random: Random) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(RollExecutor::class) {
        object Options : CommandOptions() {
            val expression = optionalString("expression", LocaleKeyData("${CalculatorCommand.LOCALE_PREFIX}.selectExpression"))
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val argument = args[options.expression]

        val dices = try {
            // We need to get only the arguments that *aren't* whitespace because if the user inputs...
            // 3d3 + 5
            // we only want to get the dices!
            val dices = Dice.parse(argument?.takeWhile { it == 'd' || it in '0'..'9' || it == '-' } ?: "6", 100)

            if (dices.isEmpty())
                throw UnsupportedOperationException("No valid dices found!")

            dices
        } catch (e: Dice.Companion.TooManyDicesException) {
            context.fail(
                context.locale["${RollCommand.LOCALE_PREFIX}.tooMuchDices"],
                emotes.loriSob
            ) { isEphemeral = true }
        } catch (e: Dice.Companion.LowerBoundHigherThanUpperBoundException) {
            context.fail(
                context.locale["${RollCommand.LOCALE_PREFIX}.invalidBound"],
                emotes.loriShrug
            ) { isEphemeral = true }
        } catch (e: IllegalArgumentException) {
            context.fail(
                context.locale["${RollCommand.LOCALE_PREFIX}.invalidBound"],
                emotes.loriShrug
            ) { isEphemeral = true }
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

        var expression: String? = null

        if (argument != null && argument.count { it != 'd' && it !in '0'..'9' && it != '-' } > 1) {
            // This may be a expression!
            expression = argument.dropWhile { !(it != 'd' && it !in '0'..'9' && it != '-') }

            response += " = ${finalResult.toInt()} `${expression.trim()}"

            finalResult = MathUtils.evaluate(finalResult.toString() + expression).toFloat()

            response += " = ${finalResult.toInt()}`"
        }

        response = if (rolledSides.size == 1 && expression == null) {
            ""
        } else {
            "`${finalResult.toInt()}` **»** $response"
        }

        val upperBound = dices.first().upperBound

        val message = context.locale["${RollCommand.LOCALE_PREFIX}.result", context.user.asMention, upperBound.toString(), finalResult.toInt()]

        context.sendMessage {
            styled(message, prefix = "\uD83C\uDFB2")

            if (response.isNotEmpty())
                styled(content = response, prefix = "\uD83E\uDD13")
        }
    }
}