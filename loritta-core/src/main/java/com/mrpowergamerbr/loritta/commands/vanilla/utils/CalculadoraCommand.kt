package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.modules.InviteLinkModule
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale


class CalculadoraCommand : AbstractCommand("calc", listOf("calculadora", "calculator"), CommandCategory.UTILS) {
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

			try {
				val result = LorittaUtils.evalMath(expression)

				context.reply(
						locale["CALC_RESULT", result],
						"\uD83E\uDD13"
				)
			} catch (e: Exception) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["CALC_INVALID", expression])
			}
		} else {
			this.explain(context)
		}
	}
}