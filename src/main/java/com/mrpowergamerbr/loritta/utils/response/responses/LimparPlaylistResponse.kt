package com.mrpowergamerbr.loritta.utils.response.responses

import java.util.regex.Pattern

class LimparPlaylistResponse : RegExResponse() {
	init {
		patterns.add("limpa|tirar|remover|excluir".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("(playlist|fila|queue)".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("\\?".toPattern(Pattern.CASE_INSENSITIVE))

		response = "{@mention} você pode limpar a playlist de música atual usando `+tocar limpar`! \uD83D\uDE09"
	}
}