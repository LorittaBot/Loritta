package net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities

import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.api.entities.Message
import net.perfectdreams.loritta.morenitta.api.entities.MessageChannel
import net.perfectdreams.loritta.morenitta.messages.LorittaMessage

class DiscordMessageChannel(handle: net.dv8tion.jda.api.entities.MessageChannel) : DiscordChannel(handle),
    MessageChannel {
	override suspend fun sendMessage(message: LorittaMessage): Message {
		return DiscordMessage(
				handle.sendMessage(message.content).await()
		)
	}

	override suspend fun sendFile(bytes: ByteArray, fileName: String, message: LorittaMessage): Message {
		return DiscordMessage(
				handle.sendMessage(message.content)
						.addFile(bytes, fileName)
						.await()
		)
	}
}