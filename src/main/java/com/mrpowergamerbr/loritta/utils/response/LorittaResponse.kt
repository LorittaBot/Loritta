package com.mrpowergamerbr.loritta.utils.response

import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

interface LorittaResponse {
	fun handleResponse(event: LorittaMessageEvent, message: String): Boolean

	fun getResponse(event: LorittaMessageEvent, message: String): String?
}