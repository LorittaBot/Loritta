package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class QualidadeCommand : CommandBase() {
	override fun getLabel(): String {
		return "qualidade"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["QUALIDADE_DESCRIPTION"]
	}

	override fun getUsage(): String {
		return "<mensagem>"
	}

	override fun getExample(): List<String> {
		return listOf("qualidade & sincronia")
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("mensagem" to "A mensagem que você deseja transformar")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			val qualidade = context.args.joinToString(" ").toCharArray().joinToString(" ").toUpperCase()
			context.reply(
					LoriReply(message = qualidade, prefix = "✍")
			)
		} else {
			this.explain(context)
		}
	}
}