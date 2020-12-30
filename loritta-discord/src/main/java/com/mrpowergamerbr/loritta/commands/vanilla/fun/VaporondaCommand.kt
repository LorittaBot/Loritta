package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.misc.VaporwaveUtils
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply

class VaporondaCommand : AbstractCommand("vaporonda", listOf("vaporwave"), category = CommandCategory.FUN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["commands.fun.vaporwave.description"]
	}

	override fun getUsage(): String {
		return "<mensagem>"
	}

	override fun getExamples(): List<String> {
		return listOf("Windows 95")
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("mensagem" to "A mensagem que você deseja transformar")
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val vaporwave = VaporwaveUtils.vaporwave(context.args.joinToString(" "))
					.escapeMentions()

			context.reply(
                    LorittaReply(message = vaporwave, prefix = "✍")
			)
		} else {
			this.explain(context)
		}
	}
}