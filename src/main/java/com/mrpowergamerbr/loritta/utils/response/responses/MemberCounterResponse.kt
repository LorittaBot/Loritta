package com.mrpowergamerbr.loritta.utils.response.responses

import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.utils.LoriReply
import java.util.regex.Pattern

class MemberCounterResponse : RegExResponse() {
	init {
		patterns.add("ativ|coloc|adicio".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("(contador|counter)".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("\\?".toPattern(Pattern.CASE_INSENSITIVE)) }

	override fun getResponse(event: LorittaMessageEvent, message: String): String? {
		val replies = listOf(
				LoriReply(
						"**Ativar o contador de membros é bem fácil!**",
						prefix = "<:lori_pac:503600573741006863>"
				),
				LoriReply(
						"Vá no painel de administração clicando aqui <https://loritta.website/dashboard> e escolha o servidor que você deseja ativar o contador de membros!",
						mentionUser = false
				),
				LoriReply(
						"Clique em \"Contador de Membros\"",
						mentionUser = false
				),
				LoriReply(
						"Procure o canal que você deseja ativar o contador e, na caixinha de texto, coloque \"{counter}\" e salve",
						mentionUser = false
				),
				LoriReply(
						"Agora é só esperar alguém entrar no seu servidor e ver a mágica acontecer!",
						prefix = "<:lori_owo:417813932380520448>",
						mentionUser = false
				)
		)

		return replies.joinToString("\n", transform = { it.build(event.author)} )
	}
}