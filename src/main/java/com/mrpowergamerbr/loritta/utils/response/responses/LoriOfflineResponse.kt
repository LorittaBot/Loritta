package com.mrpowergamerbr.loritta.utils.response.responses

import java.util.regex.Pattern

class LoriOfflineResponse : RegExResponse() {
	init {
		patterns.add("lori".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("((off|on)|caiu)".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("\\?".toPattern(Pattern.CASE_INSENSITIVE))

		response = "{@mention} se eu estou te respondendo agora, quer dizer que eu estou online, firme e forte! <:lori_yum:414222275223617546> (tá, talvez não tão forte assim \uD83E\uDD37)"
	}
}