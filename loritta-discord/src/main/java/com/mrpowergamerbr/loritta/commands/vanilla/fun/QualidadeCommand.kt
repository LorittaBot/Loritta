package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale

class QualidadeCommand : AbstractCommand("qualidade", category = CommandCategory.FUN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["QUALIDADE_DESCRIPTION"]
	}

	override fun getUsage(): String {
		return "<mensagem>"
	}

	override fun getExamples(): List<String> {
		return listOf("qualidade & sincronia")
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("mensagem" to "A mensagem que você deseja transformar")
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
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