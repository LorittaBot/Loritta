package net.perfectdreams.loritta.platform.nodejs.entities

import net.perfectdreams.loritta.api.entities.Message

class ErisMessage(val message: eris.Message) : Message {
	override val author = ErisUser(message.author)
	override val content = message.content
	override val mentionedUsers = message.mentions.map { ErisUser(it) }
	override val channel = ErisMessageChannel(message.channel)
	override suspend fun delete() {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
}