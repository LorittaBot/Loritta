package com.mrpowergamerbr.loritta.utils.response.responses

import java.util.regex.Pattern

class LanguageResponse : RegExResponse() {
	init {
		patterns.add("troca|change|troco|altero|alterar".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("(linguagem|língua|language|lingua|português|inglês|portugues|pt-br|pt-pt)".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("\\?".toPattern(Pattern.CASE_INSENSITIVE))

		response = "{@mention} você pode alterar a minha linguagem usando `+language` e escolhendo a linguagem que você queira! \uD83D\uDE09"
	}
}