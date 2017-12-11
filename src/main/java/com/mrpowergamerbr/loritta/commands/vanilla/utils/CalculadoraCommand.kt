package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat


class CalculadoraCommand : CommandBase("calc", listOf("calculadora", "calculator")) {
	override fun getUsage(): String {
		return "conta"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.CALC_DESCRIPTION
	}

	override fun getExample(): List<String> {
		return listOf("2 + 2");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS;
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val expression = context.args.joinToString(" ");
			try {
				val result = LorittaUtils.evalMath(expression)

				context.sendMessage(context.getAsMention(true) + context.locale.CALC_RESULT.msgFormat(result))
			} catch (e: Exception) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.CALC_INVALID.msgFormat(expression))
			}
		} else {
			this.explain(context)
		}
	}
}