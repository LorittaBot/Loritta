package com.mrpowergamerbr.loritta.utils.response.responses

import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.utils.LoriReply
import java.util.regex.Pattern

class HelpMeResponse : RegExResponse() {
	init {
		patterns.add("algu?(e|é)?m".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("ajud|d(ú|u)vida".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("\\?".toPattern(Pattern.CASE_INSENSITIVE))
	}

	override fun getResponse(event: LorittaMessageEvent, message: String): String? {
		if (!message.contains("399301696892829706")) {
			val replies = listOf(
					LoriReply(
							"Psiu! Se você está com uma dúvida, escreva a sua dúvida no chat e marque o cargo do `@\uD83D\uDC81\uD83D\uDCD1 Suporte da Lori`!",
							prefix = "<:lori_thinking:346810804613414912>"
					)
			)

			return replies.joinToString("\n", transform = { it.build(event.author)} )
		}
		return null
	}
}