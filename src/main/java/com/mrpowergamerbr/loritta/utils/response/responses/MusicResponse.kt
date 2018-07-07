package com.mrpowergamerbr.loritta.utils.response.responses

import com.mrpowergamerbr.loritta.Loritta
import java.util.regex.Pattern

class MusicResponse : RegExResponse() {
	init {
		patterns.add("ativa|coloca".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("(música|musica|music)".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("\\?".toPattern(Pattern.CASE_INSENSITIVE))

		response = "{@mention} para você ativar o meu sistema de música, vá no painel <${Loritta.config.websiteUrl}dashboard>, escolha o seu servidor, clique em \"DJ Loritta\", configure o canal que será usado para músicas, use \"+play\" e curta o batidão! <:loritta_quebrada:338679008210190336>"
	}
}