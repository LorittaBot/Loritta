package net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities

import net.perfectdreams.loritta.deviousfun.MessageBuilder
import net.perfectdreams.loritta.deviousfun.await
import net.perfectdreams.loritta.morenitta.api.entities.Message
import net.perfectdreams.loritta.morenitta.api.entities.MessageChannel
import net.perfectdreams.loritta.morenitta.messages.LorittaMessage

class DiscordMessageChannel(handle: net.perfectdreams.loritta.deviousfun.entities.Channel) : DiscordChannel(handle),
	MessageChannel {
	override suspend fun sendMessage(message: LorittaMessage): Message {
		return DiscordMessage(
			handle.sendMessage(message.content).await()
		)
	}

	override suspend fun sendFile(bytes: ByteArray, fileName: String, message: LorittaMessage): Message {
		println("Sending file with name $fileName")

		return DiscordMessage(
			handle.sendMessage(
				MessageBuilder(message.content)
					.addFile(bytes, fileName)
					.build()
			).await()
		)
	}
}