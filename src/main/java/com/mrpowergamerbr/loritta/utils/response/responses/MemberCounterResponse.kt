package com.mrpowergamerbr.loritta.utils.response.responses

import java.util.regex.Pattern

class MemberCounterResponse : RegExResponse() {
	init {
		patterns.add("ativ|coloc".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("(contador|counter)".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("\\?".toPattern(Pattern.CASE_INSENSITIVE))

		response = "{@mention} para você ativar o sistema de contador de membros, vá no meu painel de administração <https://loritta.website/dashboard>, escolha o seu servidor, clique em \"Contador de Membros\", procure qual canal você quer que tenha o contador e coloque \"{counter}\" lá no painel. Agora é só salvar e esperar alguém entrar no seu servidor e ver a mágica acontecer! <:lori_owo:417813932380520448>"
	}
}