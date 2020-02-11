package com.mrpowergamerbr.loritta.utils.response

import com.mrpowergamerbr.loritta.events.LorittaMessageEvent

interface LorittaResponse {
	fun handleResponse(event: LorittaMessageEvent, message: String): Boolean

	fun getResponse(event: LorittaMessageEvent, message: String): String?

	fun getPriority(): Int {
		return 0
	}
}