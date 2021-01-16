package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.misc.VaporwaveUtils
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply

class VaporQualidadeCommand : AbstractCommand("vaporqualidade", category = CommandCategory.FUN) {
	override fun getDescriptionKey() = LocaleKeyData("commands.fun.vaporquality.description")

	// TODO: Fix Usage
	// TODO: Fix Detailed Usage

	override fun getExamples(): List<String> {
		return listOf("kk eae men, o sam é brabo")
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val qualidade = VaporwaveUtils.vaporwave(context.args.joinToString(" ").toCharArray().joinToString(" ")).toUpperCase()
					.escapeMentions()

			context.reply(
                    LorittaReply(message = qualidade, prefix = "✍")
			)
		} else {
			this.explain(context)
		}
	}
}