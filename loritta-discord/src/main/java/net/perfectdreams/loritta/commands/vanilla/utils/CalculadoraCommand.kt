package net.perfectdreams.loritta.commands.vanilla.utils

import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.utils.Emotes

class CalculadoraCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("calc", "calculadora", "calculator", "calcular", "calculate"), CommandCategory.UTILS) {
	companion object {
		const val LOCALE_PREFIX = "commands.utils.calc"
	}

	override fun command() = create {
		usage {
			argument(ArgumentType.TEXT) {}
		}

		localizedDescription("$LOCALE_PREFIX.description")
		localizedExamples("$LOCALE_PREFIX.examples")

		executesDiscord {
			val context = this

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
						return@executesDiscord
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
				explain()
			}
		}
	}
}