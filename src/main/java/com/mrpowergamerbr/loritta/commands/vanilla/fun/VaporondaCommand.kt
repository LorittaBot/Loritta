package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.misc.VaporwaveUtils

class VaporondaCommand : CommandBase() {
	override fun getLabel(): String {
		return "vaporonda"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["VAPORONDA_DESCRIPTION"]
	}

	override fun getUsage(): String {
		return "<mensagem>"
	}

	override fun getExample(): List<String> {
		return listOf("Windows 95")
	}

	override fun getAliases(): List<String> {
		return listOf("vaporwave")
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("mensagem" to "A mensagem que você deseja transformar")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			val vaporwave = VaporwaveUtils.vaporwave(context.args.joinToString(" "))
			context.reply(
					LoriReply(message = vaporwave, prefix = "✍")
			)
		} else {
			this.explain(context)
		}
	}
}