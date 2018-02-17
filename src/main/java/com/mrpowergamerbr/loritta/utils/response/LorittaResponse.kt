package net.pocketdreams.dreamchat.utils.bot

import net.dv8tion.jda.core.events.message.MessageReceivedEvent

interface LorittaResponse {
	fun handleResponse(event: MessageReceivedEvent, message: String): Boolean

	fun getResponse(event: MessageReceivedEvent, message: String): String?
}