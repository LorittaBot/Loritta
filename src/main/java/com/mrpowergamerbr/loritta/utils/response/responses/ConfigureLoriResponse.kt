package com.mrpowergamerbr.loritta.utils.response.responses

import java.util.regex.Pattern

class ConfigureLoriResponse : RegExResponse() {
	init {
		patterns.add("configu".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("lori|297153970613387264".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("\\?".toPattern(Pattern.CASE_INSENSITIVE))

		response = "{@mention} para você mexer nas configurações do seu servidor é só clicar aqui! <https://loritta.website/dashboard> <:lori_owo:417813932380520448>"
	}
}