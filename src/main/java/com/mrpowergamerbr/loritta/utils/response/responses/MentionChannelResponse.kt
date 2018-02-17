package com.mrpowergamerbr.loritta.utils.response.responses

import java.util.regex.Pattern

class MentionChannelResponse : RegExResponse() {
	init {
		patterns.add("coloco|menciono|mencionar|mention".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("#|canal|channel".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("\\?".toPattern(Pattern.CASE_INSENSITIVE))

		response = "{@mention} para você mencionar um canal de texto, escreva no chat `#nome-do-canal`, envie a mensagem, copie o que irá aparecer no chat (algo assim `<#297732013006389252>`) e coloque na mensagem!"
	}
}