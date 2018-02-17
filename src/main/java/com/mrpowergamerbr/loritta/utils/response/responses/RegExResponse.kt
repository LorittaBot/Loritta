package com.mrpowergamerbr.loritta.utils.response.responses

import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.pocketdreams.dreamchat.utils.bot.LorittaResponse
import java.util.regex.Pattern

open class RegExResponse : LorittaResponse {
	val regex = mutableListOf<String>()
	val patterns = mutableListOf<Pattern>()
	var response: String = "???"

	override fun handleResponse(event: MessageReceivedEvent, message: String): Boolean {
		for (pattern in patterns) {
			val matcher = pattern.matcher(message)

			if (!matcher.find())
				return false
		}
		return postHandleResponse(event, message)
	}

	open fun postHandleResponse(event: MessageReceivedEvent, message: String): Boolean {
		return true
	}

	override fun getResponse(event: MessageReceivedEvent, message: String): String? {
		var reply = response
		reply = reply.replace("{@mention}", event.author.asMention)
		// reply = reply.replace("{displayName}", event.player.displayName)

		return reply
	}
}