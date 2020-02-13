package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.utils.Emotes

class CalculadoraCommand : AbstractCommand("calc", listOf("calculadora", "calculator"), CommandCategory.UTILS) {
	companion object {
		const val LOCALE_PREFIX = "commands.utils.calc"
	}

	override fun getUsage(): String {
		return "conta"
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["CALC_DESCRIPTION"]
	}

	override fun getExamples(): List<String> {
		return listOf("2 + 2")
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val expression = context.args.joinToString(" ")
					.replace("_", "")

			try {
				val result = LorittaUtils.evalMath(expression)

				context.reply(
						LoriReply(
								context.locale["$LOCALE_PREFIX.result", result]
						)
				)
			} catch (e: Exception) {
				context.reply(
						LoriReply(
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