package com.mrpowergamerbr.loritta.utils.response.responses

import java.util.regex.Pattern

class AddEmotesResponse : RegExResponse() {
	init {
		patterns.add("coloco|menciono|por|coloca".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("emote|emoji".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("entrada|saída|youtube|twitch|loritta".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("\\?".toPattern(Pattern.CASE_INSENSITIVE))

		response = "{@mention} para você colocar um emoji, escreva no chat `\\:emoji:`, envie a mensagem, copie o que apareça (irá aparecer algo assim `<:loritta:331179879582269451>`) e coloque na mensagem!"
	}
}