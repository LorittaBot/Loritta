package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.f
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import com.mrpowergamerbr.loritta.utils.remove
import java.util.*

class RollCommand : CommandBase() {
	override fun getLabel(): String {
		return "rolar"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.ROLL_DESCRIPTION.f()
	}

	override fun getAliases(): List<String> {
		return Arrays.asList("roll")
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

	override fun run(context: CommandContext) {

		var quantity = 1L
		var value: Long = 6
		var expression = ""

		if (context.args.size >= 1) {
			try {
				if (context.args[0].contains("d")) {
					val values = context.args[0].split("d")
					quantity = values[0].toLong()
					value = values[1].toLong()
				} else {
					value = context.args[0].toLong()
				}
				Loritta.random.nextLong(1, value + 1)
				if (context.args.size >= 2) {
					expression = context.args.remove(0).joinToString(" ")
					try {
						LorittaUtils.evalMath(Loritta.random.nextLong(1, value + 1).toString() + expression).toInt().toString()
					} catch (ex: RuntimeException) {
						context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + context.locale.get("CALC_INVALID", expression))
						return
					}

				}
			} catch (e: Exception) {
				context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + context.locale.INVALID_NUMBER.f(context.args[0]))
				return
			}

		}

		if (0 >= value) {
			context.sendMessage(context.getAsMention(true) + context.locale.ROLL_INVALID_NUMBER.f())
			return
		}

		val rolled = mutableListOf<String>()

		for (i in 1..quantity) {
			rolled.add(LorittaUtils.evalMath(Loritta.random.nextLong(1, value + 1).toString() + expression).toInt().toString())
		}

		val result = rolled.joinToString(", ")
		context.sendMessage(context.getAsMention(true) + "\uD83C\uDFB2 **${context.locale.ROLL_RESULT.f()}:** " + result)
	}
}