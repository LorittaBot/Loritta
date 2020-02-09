package net.perfectdreams.loritta.platform.frontend.entities

import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.MessageChannel
import net.perfectdreams.loritta.api.entities.User

class JSMessage(override val author: User, override val content: String, override val channel: MessageChannel) : Message {
	override val mentionedUsers: List<User>
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

	override suspend fun delete() {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
}