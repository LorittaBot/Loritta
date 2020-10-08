package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.commands.vanilla.utils.CalculadoraCommand
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.remove
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.GenericReplies

class RollCommand : AbstractCommand("roll", listOf("rolar", "dice", "dado"), CommandCategory.FUN) {
	companion object {
		private const val LOCALE_PREFIX = "commands.fun.roll"
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["$LOCALE_PREFIX.description"]
	}

	override fun getUsage(locale: LegacyBaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.NUMBER) {
				optional = true
				defaultValue = "6"
				explanation = locale.toNewLocale()["$LOCALE_PREFIX.howMuchSides"]
			}
		}
	}

	override fun getExamples(locale: LegacyBaseLocale): List<String> {
		return listOf("", "12", "24", "2d20", "3d5", "4d10", "5..10", "5..10d10")
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		var quantity = 1L
		var lowerBound = 1L
		var upperBound = 6L
		var expression = ""

		if (context.args.isNotEmpty()) {
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

				val joinedArgs = context.args.joinToString(" ")

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
						LorittaUtils.evalMath(Loritta.RANDOM.nextLong(lowerBound, upperBound + 1).toString() + expression).toInt().toString()
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
				GenericReplies.invalidNumber(context, expression)
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

			finalResult = LorittaUtils.evalMath(finalResult.toString() + expression).toFloat()

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