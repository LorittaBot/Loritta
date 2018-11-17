package com.mrpowergamerbr.loritta.utils.response.responses

import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.utils.LoriReply
import java.util.regex.Pattern

class ReceiveDreamsResponse : RegExResponse() {
	init {
		patterns.add("conseg".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("sonhos".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("\\?".toPattern(Pattern.CASE_INSENSITIVE)) }

	override fun getResponse(event: LorittaMessageEvent, message: String): String? {
		val replies = listOf(
				LoriReply(
						"**Você pode conseguir sonhos... dormindo!**",
						prefix = "<:lori_pac:503600573741006863>"
				),
				LoriReply(
						"Brincadeirinha!! ^-^ Você pode pegar sonhos usando `+daily`",
								prefix = "<:lori_owo:417813932380520448>"
				)
		)

		return replies.joinToString("\n", transform = { it.build(event.author)} )
	}
}