package com.mrpowergamerbr.loritta.utils.response.responses

import java.util.regex.Pattern

class SendFanArtsResponse : RegExResponse() {
	init {
		patterns.add("envio|envia|coloco|coloca|mando|manda".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("fan( )?arts".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("\\?".toPattern(Pattern.CASE_INSENSITIVE))

		response = "{@mention} você pode enviar fan arts no <#297732013006389252> (e marque o MrPowerGamerBR#4185 para que ele possa ver e colocar no website... caso eu fique bonita! <:lori_yum:414222275223617546>) ou envie via mensagem privada para o `MrPowerGamerBR#4185`!)"
	}
}