package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.VaporwaveUtils

class VaporondaCommand : CommandBase() {
	override fun getLabel(): String {
		return "vaporonda"
	}

	override fun getDescription(): String {
		return "Cria uma mensagem com ａｅｓｔｈｅｔｉｃｓ"
	}

	override fun getUsage(): String {
		return "<mensagem>"
	}

	override fun getExample(): List<String> {
		return listOf("Windows 95")
	}

	override fun getAliases(): List<String> {
		return listOf("vaporwave", "vapor")
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("mensagem" to "A mensagem que você deseja transformar")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			val vaporwave = VaporwaveUtils.vaporwave(context.args.joinToString(" ").toLowerCase())
			context.sendMessage(context.getAsMention(true) + vaporwave)
		} else {
			this.explain(context)
		}
	}
}