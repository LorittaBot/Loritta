package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.misc.VaporwaveUtils

class VaporQualidadeCommand : AbstractCommand("vaporqualidade", category = CommandCategory.FUN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["VAPORQUALIDADE_DESCRIPTION"]
	}

	override fun getUsage(): String {
		return "<mensagem>"
	}

	override fun getExample(): List<String> {
		return listOf("kk eae men, o sam é brabo")
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("mensagem" to "A mensagem que você deseja transformar")
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val qualidade = VaporwaveUtils.vaporwave(context.args.joinToString(" ").toCharArray().joinToString(" ")).toUpperCase()
			context.reply(
					LoriReply(message = qualidade, prefix = "✍")
			)
		} else {
			this.explain(context)
		}
	}
}