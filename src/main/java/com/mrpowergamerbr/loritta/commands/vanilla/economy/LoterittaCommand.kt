package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import com.mrpowergamerbr.loritta.utils.stripNewLines
import com.mrpowergamerbr.loritta.utils.substringIfNeeded

class LoterittaCommand : AbstractCommand("loteritta", listOf("loteria"), CommandCategory.SOCIAL) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["LOTERIA_Description"];
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		context.reply(
				LoriReply(
						"**Loteritta**",
						"<:loritta:331179879582269451>"
				),
				LoriReply(
						"Prêmio atual: **0 Sonhos**",
						"<:twitt_starstruck:352216844603752450>",
						mentionUser = false
				),
				LoriReply(
						"Tickets comprados: **0 Tickets**",
						"\uD83C\uDFAB",
						mentionUser = false
				),
				LoriReply(
						"Pessoas participando: **0 Pessoas**",
						"\uD83D\uDC65",
						mentionUser = false
				)
		)
	}
}