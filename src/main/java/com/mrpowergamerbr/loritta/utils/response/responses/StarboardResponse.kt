package com.mrpowergamerbr.loritta.utils.response.responses

import com.mrpowergamerbr.loritta.Loritta
import java.util.regex.Pattern

class StarboardResponse : RegExResponse() {
	init {
		patterns.add("ativa|usa|serve".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("star( )?board".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("\\?".toPattern(Pattern.CASE_INSENSITIVE))

		response = "{@mention}, o starboard serve como um sistema para que membros do seu servidor possam \"fixar\" mensagens que eles acharam legais/interessantes/divertidas, a mensagem irá ir automaticamente para o starboard quando ela tiver X reações de estrelas ⭐ na mensagem! Você pode configurar no Starboard no meu painel! <${Loritta.config.websiteUrl}dashboard>"
	}
}