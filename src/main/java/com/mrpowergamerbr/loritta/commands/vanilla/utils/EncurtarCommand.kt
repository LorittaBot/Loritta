package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.webpaste.TemmieBitly

class EncurtarCommand : CommandBase() {
	override fun getLabel(): String {
		return "encurtar"
	}

	override fun getUsage(): String {
		return "link"
	}

	override fun getAliases(): List<String> {
		return listOf("bitly")
	}

	override fun getExample(): List<String> {
		return listOf("https://mrpowergamerbr.com/", "https://loritta.website/")
	}

	override fun getDescription(): String {
		return "Encurta um link usando o bit.ly"
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			val temmie = TemmieBitly("R_fb665e9e7f6a830134410d9eb7946cdf", "o_5s5av92lgs")
			var short =temmie.shorten(context.args[0]);
			if (short != null && short != "INVALID_URI") {
				context.sendMessage(context.getAsMention(true) + short)
			} else {
				context.sendMessage(LorittaUtils.ERROR + " | " + context.getAsMention(true) + "A URL `${context.args[0]}` é inválida!")
			}
		} else {
			context.explain()
		}
	}
}