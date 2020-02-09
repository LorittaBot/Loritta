package net.perfectdreams.loritta.platform.frontend.entities

import kotlinx.html.div
import kotlinx.html.dom.append
import net.perfectdreams.loritta.api.entities.Member
import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.MessageChannel
import net.perfectdreams.loritta.api.messages.LorittaMessage
import org.w3c.dom.HTMLDivElement

class JSMessageChannel(val console: HTMLDivElement) : MessageChannel {
	override val name: String?
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val participants: List<Member>
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

	override suspend fun sendMessage(message: LorittaMessage): Message {
		console.append {
			div {
				+ message.content
			}
		}
		return JSMessage(
				JSUser("Loritta"),
				message.content,
				this
		)
	}
}