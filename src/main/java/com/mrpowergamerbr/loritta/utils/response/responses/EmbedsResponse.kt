package com.mrpowergamerbr.loritta.utils.response.responses

import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.utils.LoriReply
import java.util.regex.Pattern

class EmbedsResponse : RegExResponse() {
	init {
		patterns.add("ativ|coloc|uso".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("(embed)".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("\\?".toPattern(Pattern.CASE_INSENSITIVE)) }

	override fun getResponse(event: LorittaMessageEvent, message: String): String? {
		val replies = listOf(
				LoriReply(
						"**Existem vários jeitos de você utilizar embeds!**",
						prefix = "<:lori_pac:503600573741006863>"
				),
				LoriReply(
						"Veja como usar embeds no meu website! <https://loritta.website/extras/embeds>"
				),
				LoriReply(
						"Você pode usar embeds em qualquer mensagem do painel! Apenas substitua o conteúdo da mensagem pelo o código do mini tutorial acima!"
				),
				LoriReply(
						"Você também pode usar os mesmos códigos no \"+say\"!"
				)
		)

		return replies.joinToString("\n", transform = { it.build(event.author)} )
	}
}