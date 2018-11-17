package com.mrpowergamerbr.loritta.utils.response.responses

import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.utils.LoriReply
import java.util.regex.Pattern

class JoinLeaveResponse : RegExResponse() {
	init {
		patterns.add("ativ|coloc|uso".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("(entra|sai|saí)".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("\\?".toPattern(Pattern.CASE_INSENSITIVE)) }

	override fun getResponse(event: LorittaMessageEvent, message: String): String? {
		val replies = listOf(
				LoriReply(
						"**Ativar as mensagens de entrada e saída é bem fácil!**",
						prefix = "<:lori_pac:503600573741006863>"
				),
				LoriReply(
						"Vá no painel de administração clicando aqui <https://loritta.website/dashboard> e escolha o servidor que você deseja ativar as mensagens!"
				),
				LoriReply(
						"Clique em \"Mensagens de Entrada/Saída\""
				),
				LoriReply(
						"Agora é só configurar do jeito que você queira! <:eu_te_moido:366047906689581085>"
				),
				LoriReply(
						"(Dica: Se você quiser fazer aquelas mensagens bonitinhas quadradas, veja como elas funcionam aqui! <https://loritta.website/extras/embeds>",
						prefix = "<:lori_owo:417813932380520448>"
				)
		)

		return replies.joinToString("\n", transform = { it.build(event.author)} )
	}
}