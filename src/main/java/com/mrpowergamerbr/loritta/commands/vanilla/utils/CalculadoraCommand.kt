package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils


class CalculadoraCommand : CommandBase() {
	override fun getLabel(): String {
		return "calc"
	}

	override fun getUsage(): String {
		return "conta"
	}

	override fun getDescription(): String {
		return "Calcula uma expressão aritmética"
	}

	override fun getExample(): List<String> {
		return listOf("2 + 2");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS;
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			val expression = context.args.joinToString(" ");
			try {
				val result = LorittaUtils.evalMath(expression)

				context.sendMessage(context.getAsMention(true) + "Resultado: `$result`")
			} catch (e: Exception) {
				context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + "`$expression` não é uma expressão artimética válida!")
			}
		} else {
			this.explain(context)
		}
	}
}