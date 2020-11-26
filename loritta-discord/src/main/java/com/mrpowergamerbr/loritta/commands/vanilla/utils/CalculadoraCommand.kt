package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.utils.Emotes

class CalculadoraCommand : AbstractCommand("calc", listOf("calculadora", "calculator", "calcular", "calculate"), CommandCategory.UTILS) {
	companion object {
		const val LOCALE_PREFIX = "commands.utils.calc"
	}

	override fun getUsage(): String {
		return "conta"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["$LOCALE_PREFIX.description"]
	}

	override fun getExamples(): List<String> {
		return listOf(
				"2 + 2",
				"40 - 10",
				"5 * 5",
				"100 / 5",
				"(sqrt(10) * 4) / 12",
				"cos(0)",
				"sin(90)",
				"tan(45)",
				"10 % 2"
		)
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val expression = context.args.joinToString(" ")
					.replace("_", "")

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

					val resultNumber0 = LorittaUtils.evalMath(number0)
					val resultNumber1 = LorittaUtils.evalMath(number1)
					val resultNumber2 = LorittaUtils.evalMath(number2)

					// resultNumber0 --- resultNumber1
					// resultNumber2 --- x
					context.reply(
                            LorittaReply(
                                    context.locale["$LOCALE_PREFIX.result", (resultNumber2 * resultNumber1) / resultNumber0]
                            )
					)
					return
				}

				val result = LorittaUtils.evalMath(expression)

				context.reply(
                        LorittaReply(
                                context.locale["$LOCALE_PREFIX.result", result]
                        )
				)
			} catch (e: Exception) {
				context.reply(
                        LorittaReply(
                                context.locale["$LOCALE_PREFIX.invalid", expression.stripCodeMarks()] + " ${Emotes.LORI_CRYING}",
                                Emotes.LORI_HM
                        )
				)
			}
		} else {
			this.explain(context)
		}
	}
}
