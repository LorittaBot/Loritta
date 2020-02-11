package net.perfectdreams.loritta.platform.nodejs.entities

import eris.FileOptions
import eris.TextChannel
import kotlinx.coroutines.await
import net.perfectdreams.loritta.api.entities.Member
import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.MessageChannel
import net.perfectdreams.loritta.api.messages.LorittaMessage
import nodecanvas.toBuffer

class ErisMessageChannel(val textChannel: TextChannel) : MessageChannel {
	override suspend fun sendMessage(message: LorittaMessage): Message {
		return ErisMessage(textChannel.createMessage(message.content).await())
	}

	override suspend fun sendFile(bytes: ByteArray, fileName: String, message: LorittaMessage): Message {
		return ErisMessage(textChannel.createMessage(message.content, FileOptions(bytes.toBuffer(), fileName)).await())
	}

	override val name: String?
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val participants: List<Member>
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

}