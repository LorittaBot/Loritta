package net.perfectdreams.loritta.api.commands

import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.User
import net.perfectdreams.loritta.api.messages.LorittaMessage
import net.perfectdreams.loritta.api.messages.LorittaReply

abstract class CommandContext(val args: List<String>, val message: Message) {
	suspend fun sendMessage(content: String) = message.channel.sendMessage(content)
	suspend fun sendMessage(lorittaMessage: LorittaMessage) = message.channel.sendMessage(lorittaMessage)

	abstract suspend fun user(argument: Int): User?

	suspend fun reply(vararg replies: LorittaReply) = reply(replies.toList())
	suspend fun reply(replies: List<LorittaReply>): Message {
		val message = StringBuilder()
		for (loriReply in replies) {
			message.append(loriReply.build(this))
			message.append("\n")
		}
		return sendMessage(message.toString())
	}

	fun getUserMention(addSpace: Boolean): String {
		return message.author.asMention + (if (addSpace) " " else "")
	}
}