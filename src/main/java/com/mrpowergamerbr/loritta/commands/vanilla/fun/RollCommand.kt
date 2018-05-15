package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.remove

class RollCommand : AbstractCommand("roll", listOf("rolar", "dice", "dado"), CommandCategory.FUN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["ROLL_DESCRIPTION"]
	}

	override fun getUsage(): String {
		return "[número]"
	}

	override fun getExample(): List<String> {
		return listOf("", "12", "24", "2d20", "3d5", "4d10")
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("número" to "*(Opcional)* Quantos lados o dado que eu irei rolar terá, padrão: 6")
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var quantity = 1L
		var value: Long = 6
		var expression = ""

		if (context.args.isNotEmpty()) {
			try {
				if (context.args[0].contains("d")) {
					val values = context.args[0].split("d")

					quantity = values[0].toLongOrNull() ?: 1
					value = values[1].toLong()
				} else {
					value = context.args[0].toLong()
				}
				Loritta.RANDOM.nextLong(1, value + 1)
				if (context.args.size >= 2) {
					expression = context.args.remove(0).joinToString(" ")
					try {
						LorittaUtils.evalMath(Loritta.RANDOM.nextLong(1, value + 1).toString() + expression).toInt().toString()
					} catch (ex: RuntimeException) {
						context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.get("CALC_INVALID", expression))
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

		if (0 >= value) {
			context.sendMessage(context.getAsMention(true) + locale["ROLL_INVALID_NUMBER"])
			return
		}

		val rolledSides = mutableListOf<Long>()

		var response = ""
		for (i in 1..quantity) {
			val rolledSide = Loritta.RANDOM.nextLong(1, value + 1)
			rolledSides.add(rolledSide)
		}

		response = rolledSides.joinToString(" + ")

		var finalResult = 0F

		rolledSides.forEach {
			finalResult += it
		}

		if (expression.isNotEmpty()) {
			response += " = ${finalResult.toInt()} `${expression.trim()}";

			finalResult = LorittaUtils.evalMath(finalResult.toString() + expression).toFloat()

			response += " = ${finalResult.toInt()}`"
		}

		if (rolledSides.size == 1 && expression.isEmpty()) {
			response = ""
		} else {
			response = "`${finalResult.toInt()}` **»** $response"
		}

		var message = context.locale["ROLL_RESULT", value, finalResult.toInt()]

		val list = mutableListOf<LoriReply>()
		list.add(LoriReply(message = message, prefix = "\uD83C\uDFB2", forceMention = true))

		if (response.isNotEmpty()) {
			list.add(LoriReply(message = response, prefix = "\uD83E\uDD13", mentionUser = false))
		}

		context.reply(*list.toTypedArray())
	}
}