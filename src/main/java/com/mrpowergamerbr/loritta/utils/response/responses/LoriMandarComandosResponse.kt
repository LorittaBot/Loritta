package com.mrpowergamerbr.loritta.utils.response.responses

import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.utils.LoriReply
import java.util.regex.Pattern

class LoriMandarComandosResponse : RegExResponse() {
	init {
		patterns.add("enviando|mandando|mandar|responde".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("comando|cmd".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("\\?".toPattern(Pattern.CASE_INSENSITIVE))
	}

	override fun getResponse(event: LorittaMessageEvent, message: String): String? {
		val replies = listOf(
				LoriReply(
						"**Vamos ir por partes...**",
						prefix = "<:lori_thinking:346810804613414912>"
				),
				LoriReply(
						"O que acontece ao me mencionar no seu servidor? Escreva uma mensagem *apenas* me mencionando e veja o que aparece!"
				),
				LoriReply(
						"Eu respondi? Legal! Agora leia o que eu falei lá e arrume o problema! <:lori_yum:414222275223617546> Normalmente é porque você bloqueou o canal de texto para eu não poder usar comandos, ou porque você tirou as permissões de um cargo poder usar comandos lá ou outro probleminha básico..."
				),
				LoriReply(
						"Eu não respondi? Então veja se eu tenho permissão para ler e falar no canal de texto (se eu não apareço nos membros online, provavelmente eu não tenho permissão para ler o canal!)"
				),
				LoriReply(
						"Eu não respondi mas aparece que eu estou digitando mas nunca envio nada? Então deu ruim!"
				),
				LoriReply(
						"Caso você não tenha conseguido resolver o problema, então envie uma mensagem para alguém do suporte! \uD83D\uDE09"
				)
		)

		return replies.joinToString("\n", transform = { it.build(event.author)} )
	}
}