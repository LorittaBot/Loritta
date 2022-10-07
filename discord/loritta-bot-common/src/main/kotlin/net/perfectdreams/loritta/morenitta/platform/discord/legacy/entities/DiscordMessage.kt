package net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities

import net.perfectdreams.loritta.deviousfun.await
import net.perfectdreams.loritta.morenitta.api.entities.Message
import net.perfectdreams.loritta.morenitta.api.entities.User
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities.jda.JDAUser

class DiscordMessage(val handle: net.perfectdreams.loritta.deviousfun.entities.Message) : Message {
	override val author = JDAUser(handle.author)
	override val content = handle.contentRaw
	override val mentionedUsers: List<User>
		get() = handle.mentionedUsers.map { JDAUser(it) }
	override val channel = DiscordMessageChannel(handle.channel)

	override suspend fun delete() {
		handle.delete().await()
	}
}