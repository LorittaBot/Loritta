package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.api.commands.CommandCategory

class CongaParrotCommand : AbstractCommand("congaparrot", category = CommandCategory.FUN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["CONGAPARROT_Description"]
	}

	override fun getUsage(): String {
		return "número"
	}

	override fun getExamples(): List<String> {
		return listOf("5", "10")
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		var arg0 = context.args.getOrNull(0)

		if (arg0 == null) {
			context.explain()
			return
		}

		val upTo = arg0.toIntOrNull()

		if (upTo == null) {
			context.reply(
					LoriReply(
							message = locale["INVALID_NUMBER", context.args[0]],
							prefix = Constants.ERROR
					)
			)
			return
		}

		if (upTo in 1..50) {
			var message = ""

			for (idx in 1..upTo) {
				message += "<a:congaparrot:393804615067500544>"
			}

			context.sendMessage(message)
		} else {
			context.reply(
					LoriReply(
							message = locale["CONGAPARROT_InvalidRange"],
							prefix = Constants.ERROR

					)
			)
		}
	}
}