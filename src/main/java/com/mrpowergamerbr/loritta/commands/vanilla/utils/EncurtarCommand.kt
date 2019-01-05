package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.webpaste.TemmieBitly

class EncurtarCommand : AbstractCommand("shorten", listOf("bitly", "encurtar"), CommandCategory.UTILS) {
	override fun getUsage(): String {
		return "link"
	}

	override fun getExamples(): List<String> {
		return listOf("https://mrpowergamerbr.com/", "https://loritta.website/")
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["BITLY_DESCRIPTION"]
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val temmie = TemmieBitly("R_fb665e9e7f6a830134410d9eb7946cdf", "o_5s5av92lgs")
			var url = context.args[0]
			if (!url.startsWith("http")) {
				url = "http://$url"
			}
			var short = temmie.shorten(url)
			if (short != null && short != "INVALID_URI") {
				context.reply(
						LoriReply(
								message = short,
								prefix = "\uD83D\uDDDC"
						)
				)
			} else {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.legacyLocale["BITLY_INVALID", context.args[0]])
			}
		} else {
			context.explain()
		}
	}
}