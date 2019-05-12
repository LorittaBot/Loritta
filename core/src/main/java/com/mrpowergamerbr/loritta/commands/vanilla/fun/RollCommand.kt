package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.modules.InviteLinkModule
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments

class RollCommand : AbstractCommand("roll", listOf("rolar", "dice", "dado"), CommandCategory.FUN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["commands.entertainment.roll.description"]
	}

	override fun getUsage(locale: LegacyBaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.NUMBER) {
				optional = true
				defaultValue = "6"
				explanation = locale.toNewLocale()["commands.entertainment.roll.howMuchSides"]
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
						context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.legacyLocale.get("CALC_INVALID", expression))
						return
					}
					if (!expression.startsWith(" ")) {
						expression += " " // Para deixar bonitinho
					}
				}
			} catch (e: Exception) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["INVALID_NUMBER", context.args[0]])
				return
			}

		}

		if (0 >= upperBound || lowerBound > upperBound) {
			context.sendMessage(context.getAsMention(true) + locale["ROLL_INVALID_NUMBER"])
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

		var message = context.legacyLocale["ROLL_RESULT", upperBound, finalResult.toInt()]

		val list = mutableListOf<LoriReply>()
		list.add(LoriReply(message = message, prefix = "\uD83C\uDFB2", forceMention = true))

		if (response.isNotEmpty()) {
			list.add(LoriReply(message = response, prefix = "\uD83E\uDD13", mentionUser = false))
		}

		context.reply(*list.toTypedArray())
	}
}