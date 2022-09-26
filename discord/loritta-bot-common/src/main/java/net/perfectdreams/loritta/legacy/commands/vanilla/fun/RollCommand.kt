package net.perfectdreams.loritta.legacy.commands.vanilla.`fun`

import net.perfectdreams.loritta.legacy.Loritta
import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.commands.vanilla.utils.CalculadoraCommand
import net.perfectdreams.loritta.legacy.utils.Constants
import net.perfectdreams.loritta.legacy.utils.remove
import net.perfectdreams.loritta.common.api.commands.ArgumentType
import net.perfectdreams.loritta.common.api.commands.CommandArguments
import net.perfectdreams.loritta.common.api.commands.arguments
import net.perfectdreams.loritta.common.messages.LorittaReply
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.locale.LocaleStringData
import net.perfectdreams.loritta.common.utils.math.MathUtils
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.legacy.utils.GenericReplies
import net.perfectdreams.loritta.legacy.utils.OutdatedCommandUtils

class RollCommand : AbstractCommand("roll", listOf("rolar", "dice", "dado"), net.perfectdreams.loritta.common.commands.CommandCategory.FUN) {
	companion object {
		private const val LOCALE_PREFIX = "commands.command.roll"
	}

	override fun getDescriptionKey() = LocaleKeyData("$LOCALE_PREFIX.description")

	override fun getUsage(): CommandArguments {
		return arguments {
			argument(ArgumentType.NUMBER) {
				optional = true
				defaultValue = LocaleStringData("6")
				explanation = LocaleKeyData("$LOCALE_PREFIX.howMuchSides")
			}
		}
	}

	override fun getExamplesKey() = LocaleKeyData("commands.command.roll.examples")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "roll")

		var quantity = 1L
		var lowerBound = 1L
		var upperBound = 6L
		var expression = ""

		if (context.args.isNotEmpty()) {
			val joinedArgs = context.args.joinToString(" ")

			try {
				fun setBounds(arg: String) {
					// Se o usuário inserir...
					// 5..10
					// 10
					val bounds = arg.split("..")
					if (bounds.size == 1) { // Caso não tenha nenhum "..", então só coloque o upper bound
						upperBound = bounds[0].toLong()
					} else { // Mas caso tenha (5..10), então coloque o lower bound e o upper bound!
						lowerBound = bounds[0].toLong()
						upperBound = bounds[1].toLong()
					}
				}

				if (context.args[0].contains("d")) {
					val values = context.args[0].split("d")

					quantity = values[0].toLongOrNull() ?: 1
					setBounds(values[1])
				} else {
					setBounds(context.args[0])
				}

				if (context.args.size >= 2) {
					expression = context.args.remove(0).joinToString(" ")
					try {
						MathUtils.evaluate(Loritta.RANDOM.nextLong(lowerBound, upperBound + 1).toString() + expression).toInt().toString()
					} catch (ex: RuntimeException) {
						context.reply(
                                LorittaReply(
                                        context.locale["${CalculadoraCommand.LOCALE_PREFIX}.invalid", expression] + " ${Emotes.LORI_CRYING}",
                                        Emotes.LORI_HM
                                )
						)
						return
					}
					if (!expression.startsWith(" ")) {
						expression += " " // Para deixar bonitinho
					}
				}
			} catch (e: Exception) {
				GenericReplies.invalidNumber(context, joinedArgs)
				return
			}
		}

		if (quantity > 100) {
			context.reply(
                    LorittaReply(
                            context.locale["$LOCALE_PREFIX.tooMuchDices"] + " ${Emotes.LORI_CRYING}",
                            Constants.ERROR
                    )
			)
			return
		}

		if (0 >= upperBound || lowerBound > upperBound) {
			context.reply(
                    LorittaReply(
                            context.locale["$LOCALE_PREFIX.invalidBound"] + " ${Emotes.LORI_SHRUG}",
                            Constants.ERROR
                    )
			)
			return
		}

		val rolledSides = mutableListOf<Long>()

		var response = ""
		for (i in 1..quantity) {
			val rolledSide = Loritta.RANDOM.nextLong(lowerBound, upperBound + 1)
			rolledSides.add(rolledSide)
		}

		response = rolledSides.joinToString(" + ")

		var finalResult = 0F

		rolledSides.forEach {
			finalResult += it
		}

		if (expression.isNotEmpty()) {
			response += " = ${finalResult.toInt()} `${expression.trim()}"

			finalResult = MathUtils.evaluate(finalResult.toString() + expression).toFloat()

			response += " = ${finalResult.toInt()}`"
		}

		if (rolledSides.size == 1 && expression.isEmpty()) {
			response = ""
		} else {
			response = "`${finalResult.toInt()}` **»** $response"
		}

		val message = context.locale["$LOCALE_PREFIX.result", context.getAsMention(false), upperBound.toString(), finalResult.toInt()]

		val list = mutableListOf<LorittaReply>()
		list.add(LorittaReply(message = message, prefix = "\uD83C\uDFB2", forceMention = true))

		if (response.isNotEmpty()) {
			list.add(LorittaReply(message = response, prefix = "\uD83E\uDD13", mentionUser = false))
		}

		context.reply(*list.toTypedArray())
	}
}